package com.tenacy.snaplink.util;

public class DocumentationDescriptions {

    public static final String TAG_URL_API = "URL 단축 및 관리를 위한 API 그룹입니다.";
    public static final String TAG_STATISTICS_API = "URL 사용 통계 데이터를 제공하는 API 그룹입니다.";
    public static final String TAG_METRICS_API = "시스템 성능 지표와 모니터링 정보를 제공하는 API 그룹입니다.";

    public static final String OPERATION_CREATE_SHORT_URL = "긴 URL을 짧은 형태로 변환합니다. 선택적으로 커스텀 코드와 유효기간을 지정할 수 있습니다.";
    public static final String OPERATION_GET_URL_INFO = "단축 URL의 상세 정보를 조회합니다. 원본 URL, 생성일, 만료일 등의 정보를 포함합니다.";
    public static final String OPERATION_GET_URL_STATS = "특정 단축 URL의 사용 통계를 조회합니다. 클릭 수, 브라우저별 통계, 국가별 통계 등을 제공합니다.";
    public static final String OPERATION_GET_METRICS = "시스템 전체 성능 지표를 조회합니다. 응답 시간, 처리량, 오류율 등의 정보를 포함합니다.";
    public static final String OPERATION_GET_TIMESERIES_METRICS = "시계열 형태의 성능 지표를 조회합니다. 특정 기간 동안의 성능 변화 추이를 분석할 수 있습니다.";

    public static final String REQUEST_ORIGINAL_URL = "단축하려는 원본 URL입니다. 유효한 웹 주소 형식이어야 합니다.";
    public static final String REQUEST_CUSTOM_CODE = "사용자가 직접 지정하는 단축 코드입니다. 미입력 시 시스템이 자동으로 생성합니다.";
    public static final String REQUEST_VALIDITY_IN_DAYS = "URL의 유효 기간(일 단위)입니다. 입력하지 않으면 기본값이 적용됩니다.";

    public static final String RESPONSE_ORIGINAL_URL = "단축 전 원본 URL입니다. 리다이렉트될 실제 웹 주소입니다.";
    public static final String RESPONSE_SHORT_CODE = "단축 URL의 고유 식별자로 사용되는 7자리 코드입니다.";
    public static final String RESPONSE_SHORT_URL = "생성된 단축 URL의 전체 주소입니다. 이 주소로 접속하면 원본 URL로 리다이렉트됩니다.";
    public static final String RESPONSE_CREATED_AT = "단축 URL이 생성된 정확한 날짜와 시간입니다.";
    public static final String RESPONSE_EXPIRES_AT = "단축 URL이 만료되는 날짜와 시간입니다. 이 시간 이후에는 URL이 더 이상 작동하지 않습니다.";
    public static final String RESPONSE_CLICK_COUNT = "단축 URL이 클릭된 총 횟수입니다. 사용자가 URL을 통해 리다이렉트된 모든 횟수를 집계합니다.";
    public static final String RESPONSE_CUSTOM = "사용자 정의 코드 사용 여부를 나타냅니다. true인 경우 사용자가 직접 코드를 지정했음을 의미합니다.";
    public static final String RESPONSE_DAILY_TREND = "일별 클릭 추이 데이터입니다. 최근 날짜별 클릭 수를 포함합니다.";
    public static final String RESPONSE_BROWSERS = "브라우저별 클릭 통계입니다. 각 브라우저 유형과 해당 브라우저에서의 클릭 수를 제공합니다.";
    public static final String RESPONSE_COUNTRIES = "국가별 클릭 통계입니다. 접속자의 지리적 위치에 따른 클릭 분포를 보여줍니다.";
    public static final String RESPONSE_CLICK_STATS_TOTAL_CLICKS = "단축 URL이 생성된 이후 발생한 총 클릭 수입니다.";
    public static final String RESPONSE_CLICK_STATS_DAILY_CLICKS = "오늘 하루 동안 발생한 클릭 수입니다. 매일 자정에 초기화됩니다.";
    public static final String RESPONSE_CACHE_HIT_RATIO = "캐시 적중률입니다. 전체 요청 중 캐시에서 처리된 요청의 비율을 백분율로 표시합니다.";
    public static final String RESPONSE_AVG_RESPONSE_TIME = "모든 요청의 평균 응답 시간입니다. 밀리초 단위로 표시됩니다.";
    public static final String RESPONSE_P95_RESPONSE_TIME = "95% 백분위 응답 시간입니다. 전체 요청 중 95%가 이 시간 내에 처리됨을 의미합니다.";
    public static final String RESPONSE_P99_RESPONSE_TIME = "99% 백분위 응답 시간입니다. 전체 요청 중 99%가 이 시간 내에 처리됨을 의미합니다.";
    public static final String RESPONSE_TPS = "초당 처리된 트랜잭션 수입니다. 시스템의 처리 능력을 나타내는 지표입니다.";
    public static final String RESPONSE_TOTAL_REQUESTS = "시스템 시작 이후 처리된 총 요청 수입니다.";
    public static final String RESPONSE_ERROR_RATE = "전체 요청 대비 오류가 발생한 요청의 비율입니다. 백분율로 표시됩니다.";
    public static final String RESPONSE_DB_QUERY_COUNT = "데이터베이스에 실행된 총 쿼리 수입니다. 시스템 시작 이후 집계된 값입니다.";
    public static final String RESPONSE_ACTIVE_URL_COUNT = "현재 활성화된(만료되지 않은) URL의 총 개수입니다.";

    public static final String EXAMPLE_IN_ORIGINAL_URL = "URL HERE";
    public static final String EXAMPLE_IN_VALIDITY_IN_DAYS = "30";
    public static final String EXAMPLE_ORIGINAL_URL = "https://www.google.com";
    public static final String EXAMPLE_SHORT_CODE = "uThvf9N";
    public static final String EXAMPLE_SHORT_URL = "http://localhost:8080/uThvf9N";
    public static final String EXAMPLE_CREATED_AT = "2025-08-08T20:36:57.436032";
    public static final String EXAMPLE_EXPIRES_AT = "2025-09-07T20:36:57.436032";
    public static final String EXAMPLE_CLICK_COUNT = "0";
    public static final String EXAMPLE_CUSTOM = "false";
    public static final String EXAMPLE_DAILY_TREND = "{\"2025-08-01\": 10, \"2025-08-02\": 15}";
    public static final String EXAMPLE_BROWSERS = "{\"Chrome\": 150, \"Firefox\": 50, \"Safari\": 30}";
    public static final String EXAMPLE_COUNTRIES = "{\"US\": 100, \"KR\": 80, \"JP\": 20}";
    public static final String EXAMPLE_CLICK_STATS_TOTAL_CLICKS = "250";
    public static final String EXAMPLE_CLICK_STATS_DAILY_CLICKS = "25";
    public static final String EXAMPLE_CACHE_HIT_RATIO = "85.75%";
    public static final String EXAMPLE_AVG_RESPONSE_TIME = "6.42 ms";
    public static final String EXAMPLE_P95_RESPONSE_TIME = "12.37 ms";
    public static final String EXAMPLE_P99_RESPONSE_TIME = "25.84 ms";
    public static final String EXAMPLE_TPS = "120.50";
    public static final String EXAMPLE_TOTAL_REQUESTS = "1250478";
    public static final String EXAMPLE_ERROR_RATE = "0.0025%";
    public static final String EXAMPLE_DB_QUERY_COUNT = "879654";
    public static final String EXAMPLE_ACTIVE_URL_COUNT = "45678";

    public static final String PARAM_SHORT_CODE = "단축 URL의 식별자로 사용되는 7자리 고유 코드입니다. URL 끝에 위치합니다.";

}