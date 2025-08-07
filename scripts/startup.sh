#!/bin/bash

# 사용자 설정 - 이 부분만 수정하세요
DOCKER_USERNAME="tenutz"  # Docker Hub 사용자 이름

# 데이터베이스 설정
DB_ROOT_PASSWORD="1234"

# Grafana 설정
GRAFANA_ADMIN_USER="admin"
GRAFANA_ADMIN_PASSWORD="1234"

# 색상 설정
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'  # No Color

# 로그 함수
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# 0. 스왑 메모리 설정
if swapon --show | grep -q "/swapfile"; then
    log "스왑 메모리가 이미 활성화되어 있습니다."
    free
else
    log "스왑 메모리 설정 중..."
    sudo dd if=/dev/zero of=/swapfile bs=128M count=16
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile

    # /etc/fstab에 항목이 없으면 추가
    if ! grep -q "/swapfile swap swap" /etc/fstab; then
        echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
    fi

    log "스왑 메모리 설정 완료"
    free
fi

# 1. 시스템 업데이트
log "시스템 업데이트 중..."
sudo dnf update -y

# 2. Docker 설치 (Amazon Linux 2023용)
log "Docker 설치 중..."
sudo dnf install -y docker

# 3. Docker 서비스 시작 및 활성화
log "Docker 서비스 시작 및 활성화..."
sudo systemctl start docker
sudo systemctl enable docker

# 4. 현재 사용자를 docker 그룹에 추가
log "사용자를 docker 그룹에 추가..."
sudo usermod -aG docker $USER
sudo chmod 666 /var/run/docker.sock

# 5. Docker Compose 설치
log "Docker Compose 설치 중..."
DOCKER_COMPOSE_VERSION="v2.18.1"
sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# 6. 애플리케이션 디렉토리 생성
log "애플리케이션 디렉토리 생성..."
mkdir -p ~/snaplink
cd ~/snaplink

# 7. 필요한 디렉토리 생성
log "필요한 디렉토리 생성..."
mkdir -p ./sql ./prometheus ./grafana/provisioning

# 안정적인 방법으로 EC2 메타데이터 가져오기
get_ec2_metadata() {
    local path=$1
    local result

    # IMDSv2 토큰 방식 시도
    TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" 2>/dev/null)

    if [ -n "$TOKEN" ]; then
        # IMDSv2 토큰 방식으로 메타데이터 가져오기
        result=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" "http://169.254.169.254/latest/meta-data/$path" 2>/dev/null)
    else
        # 기존 방식으로 시도
        result=$(curl -s "http://169.254.169.254/latest/meta-data/$path" 2>/dev/null)
    fi

    if [[ $result == *"<"*"html"* ]] || [[ -z "$result" ]]; then
        # HTML 응답이거나 빈 응답이면 실패로 간주
        return 1
    fi

    echo "$result"
    return 0
}

# EC2 메타데이터에서 호스트명과 IP 주소 가져오기
PUBLIC_HOSTNAME=$(get_ec2_metadata "public-hostname")
if [ $? -ne 0 ] || [ -z "$PUBLIC_HOSTNAME" ]; then
    warn "EC2 메타데이터에서 퍼블릭 호스트명을 가져오지 못했습니다. 기본값을 사용합니다."
    # 퍼블릭 IP 주소 시도
    PUBLIC_IP=$(get_ec2_metadata "public-ipv4")
    if [ $? -ne 0 ] || [ -z "$PUBLIC_IP" ]; then
        warn "EC2 메타데이터에서 퍼블릭 IP를 가져오지 못했습니다. localhost를 사용합니다."
        DOMAIN_NAME="localhost"
    else
        DOMAIN_NAME="$PUBLIC_IP"
    fi
else
    DOMAIN_NAME="$PUBLIC_HOSTNAME"
fi

log "도메인 이름: $DOMAIN_NAME"

# 8. .env 파일 생성
log ".env 파일 생성..."
cat > .env << EOF
DOCKER_USERNAME=${DOCKER_USERNAME}
DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
GRAFANA_ADMIN_USER=${GRAFANA_ADMIN_USER}
GRAFANA_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
DOMAIN_NAME=${DOMAIN_NAME}
EOF

# 9. Docker Compose 파일 생성
log "Docker Compose 파일 생성..."
cat > docker-compose.yml << EOF
version: '3.8'

services:
  app:
    image: ${DOCKER_USERNAME}/snaplink:latest
    container_name: snaplink-app
    ports:
      - "80:8080"
      - "443:8443"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/snaplink?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=${DB_ROOT_PASSWORD}
      - SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQL8Dialect
      - SPRING_JPA_DATABASE-PLATFORM=org.hibernate.dialect.MySQL8Dialect
      - SPRING_JPA_DATABASE=mysql
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
      - APP_SHORTURL_DOMAIN=http://${DOMAIN_NAME}
      - JAVA_OPTS=-Xms128m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:CompressedClassSpaceSize=64m -Xss256k -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+AlwaysPreTouch -XX:MaxGCPauseMillis=200 -XX:+DisableExplicitGC -Djava.security.egd=file:/dev/./urandom -XX:+ExitOnOutOfMemoryError
    depends_on:
      - db
      - redis
    restart: always
    deploy:
      resources:
        limits:
          cpus: '0.4'
          memory: 450M
        reservations:
          cpus: '0.2'
          memory: 350M
    volumes:
      - app-logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    networks:
      - snaplink-network
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "5"

  db:
    image: mysql:8.0
    container_name: snaplink-mysql
    environment:
      - MYSQL_DATABASE=snaplink
      - MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-authentication-plugin=mysql_native_password --max-connections=100 --innodb-buffer-pool-size=128M
    restart: always
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${DB_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - snaplink-network
    deploy:
      resources:
        limits:
          cpus: '0.3'
          memory: 256M
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "5"

  redis:
    image: redis:7.0
    container_name: snaplink-redis
    volumes:
      - redis-data:/data
    command: redis-server --maxmemory 128mb --maxmemory-policy allkeys-lru --appendonly yes --save 900 1 --save 300 10 --save 60 10000
    restart: always
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - snaplink-network
    deploy:
      resources:
        limits:
          cpus: '0.1'
          memory: 128M
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "3"

  prometheus:
    image: prom/prometheus:latest
    container_name: snaplink-prometheus
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    restart: always
    depends_on:
      - app
    networks:
      - snaplink-network
    deploy:
      resources:
        limits:
          cpus: '0.05'
          memory: 128M
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "3"

  grafana:
    image: grafana/grafana:latest
    container_name: snaplink-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_ADMIN_USER}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
      - GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    restart: always
    depends_on:
      - prometheus
    networks:
      - snaplink-network
    deploy:
      resources:
        limits:
          cpus: '0.05'
          memory: 128M
    logging:
      driver: "json-file"
      options:
        max-size: "50m"
        max-file: "3"

volumes:
  app-logs:
  mysql-data:
  redis-data:
  prometheus-data:
  grafana-data:

networks:
  snaplink-network:
    driver: bridge
EOF

# 10. Prometheus 설정 파일 생성
log "Prometheus 설정 파일 생성..."
cat > ./prometheus/prometheus.yml << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'spring-actuator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']
EOF

# 11. 시작 스크립트 생성
log "시작 스크립트 생성..."
cat > start.sh << 'EOF'
#!/bin/bash
cd ~/snaplink

# 환경 변수 불러오기
source .env

# Docker 로그인 (필요시)
if [ -n "$DOCKER_HUB_USERNAME" ] && [ -n "$DOCKER_HUB_PASSWORD" ]; then
    echo "$DOCKER_HUB_PASSWORD" | docker login -u "$DOCKER_HUB_USERNAME" --password-stdin
fi

# 최신 이미지 가져오기
docker-compose pull

# 컨테이너 실행
docker-compose up -d

# 컨테이너 상태 확인
echo "서비스 상태:"
docker-compose ps
EOF

chmod +x start.sh

# 12. 중지 스크립트 생성
log "중지 스크립트 생성..."
cat > stop.sh << 'EOF'
#!/bin/bash
cd ~/snaplink
docker-compose down
EOF

chmod +x stop.sh

# 13. 로그 확인 스크립트 생성
log "로그 확인 스크립트 생성..."
cat > logs.sh << 'EOF'
#!/bin/bash
cd ~/snaplink
docker-compose logs -f $1
EOF

chmod +x logs.sh

# 14. 서비스 시작
log "서비스 시작 중..."
./start.sh

# 15. 정보 출력
echo ""
echo "==============================================="
echo "            설치가 완료되었습니다!"
echo "==============================================="
echo ""
echo "애플리케이션 URL:"
echo "  http://${DOMAIN_NAME}"
echo "  Grafana: http://${DOMAIN_NAME}:3000"
echo ""
echo "관리 명령어:"
echo "  서비스 시작/재시작: cd ~/snaplink && ./start.sh"
echo "  서비스 중지: cd ~/snaplink && ./stop.sh"
echo "  로그 확인: cd ~/snaplink && ./logs.sh [서비스명]"
echo "    예) ./logs.sh app - 애플리케이션 로그 확인"
echo "    예) ./logs.sh db - 데이터베이스 로그 확인"
echo ""
echo "==============================================="