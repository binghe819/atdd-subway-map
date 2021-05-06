package wooteco.subway.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.AcceptanceTest;
import wooteco.subway.controller.dto.response.LineResponse;

@DisplayName("지하철 노선 관련 기능")
@Transactional
class LineAcceptanceTest extends AcceptanceTest {

    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createLine() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("color", "bg-red-600");
        params.put("name", "신분당선");

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
        assertThat(response.body().jsonPath().get("name").toString()).isEqualTo("신분당선");
    }

    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void getLines() {
        // given
        Map<String, String> param1 = new HashMap<>();
        param1.put("name", "2호선");
        param1.put("color", "bg-red-600");
        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(param1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        Map<String, String> param2 = new HashMap<>();
        param2.put("name", "3호선");
        param2.put("color", "bg-red-600");
        ExtractableResponse<Response> createResponse2 = RestAssured.given().log().all()
            .body(param2)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> expectedLineIds = Arrays.asList(createResponse1, createResponse2)
            .stream()
            .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
            .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class)
            .stream()
            .map(it -> it.getId())
            .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void showLine() {
        // given
        Map<String, String> param1 = new HashMap<>();
        param1.put("name", "4호선");
        param1.put("color", "bg-blue-600");
        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(param1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines/{id}", createResponse1.header("Location").split("/")[2])
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.body().jsonPath().get("name").toString()).isEqualTo("4호선");
        assertThat(response.body().jsonPath().get("color").toString()).isEqualTo("bg-blue-600");
    }

    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateLine() {
        // given
        Map<String, String> param1 = new HashMap<>();
        param1.put("name", "5호선");
        param1.put("color", "bg-blue-600");
        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(param1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        Map<String, String> updateParam = new HashMap<>();
        updateParam.put("name", "6호선");
        updateParam.put("color", "bg-blue-600");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(updateParam)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/{id}", createResponse1.header("Location").split("/")[2])
            .then().log().all()
            .extract();

        ExtractableResponse<Response> expectedResponse = RestAssured.given().log().all()
            .when()
            .get("/lines/{id}", createResponse1.header("Location").split("/")[2])
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(expectedResponse.body().jsonPath().get("name").toString())
            .isEqualTo(updateParam.get("name"));
        assertThat(expectedResponse.body().jsonPath().get("color").toString())
            .isEqualTo(updateParam.get("color"));
    }

    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteLine() {
        // given
        Map<String, String> param1 = new HashMap<>();
        param1.put("name", "7호선");
        param1.put("color", "bg-yellow-600");
        ExtractableResponse<Response> createResponse1 = RestAssured.given().log().all()
            .body(param1)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/lines/{id}", createResponse1.header("Location").split("/")[2])
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
