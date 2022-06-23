package org.example;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.Link;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.*;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Работа со Spoonacular API:
 * 1. Отрефакторьте код проверок и добавьте дополнительные тесты для запросов из цепочки Shopping List,
 * используя rest-assured.
 * 2. Воспользуйтесь кейсами из ПЗ № 2 и 3, перенеся всю логику из постман-коллекции в код.
 * 3. Сдайте ссылку на репозиторий, указав ветку с кодом.
 *
 * Главные критерии для проверки — отсутствие хардкода в коде тестов и наличие тестов на запросы:
 * Add to Shopping list (POST /mealplanner/:username/shopping-list/items),
 * Get Shopping List (GET /mealplanner/:username/shopping-list) и
 * Delete from Shopping list (DELETE /mealplanner/:username/shopping-list/items/:id).
 */

public class TestBackEnd
{

    private final String apiKey = "04fbf7a95bcc4e4199ceb3eea9875548";
    // api key:  "04fbf7a95bcc4e4199ceb3eea9875548"
    //           "82c9229354f849e78efe010d94150807"
    //           "db254b5cd61744d39a2deebd9c361444"

    private final String hash = "78565c31a9193281f8031cdb027aeb3cd56b5771";
    private String id;

    // Спецификации для запросов:
    RequestSpecification requestSpecification = null,           // пример валидного запроа
                         invalidRequestSpecification = null;    // пример невалидного запроса

    // Спецификации для ответов:
    ResponseSpecification responseSpecification = null,         // пример валидного ответа ("application/json")
                          responseSpecificationAlt = null,      // пример валидного ответа ("application/json;charset=utf-8")
                          invalidResponseSpecification = null;  // пример невалидного ответа

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new AllureRestAssured());
    }

    @BeforeEach
    void beforeTest() {
        // Запрос
        requestSpecification = new RequestSpecBuilder()
                .addQueryParam("apiKey", apiKey)
                .log(LogDetail.ALL)
                .build();

        // Ответ для метода Get и Post:
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectStatusLine("HTTP/1.1 200 OK")
                .expectContentType(ContentType.JSON)
                .expectResponseTime(Matchers.lessThan(7500L))
                .expectHeader("Content-Type", "application/json")
                .expectHeader("Connection", "keep-alive")
                .expectHeader("Content-Encoding", "gzip")
                .build();

        // Ответ для метода Post в mealplanner:
        responseSpecificationAlt = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectStatusLine("HTTP/1.1 200 OK")
                .expectContentType(ContentType.JSON)
                .expectResponseTime(Matchers.lessThan(7500L))
                .expectHeader("Content-Type", "application/json;charset=utf-8")
                .expectHeader("Connection", "keep-alive")
                .expectHeader("Content-Encoding", "gzip")
                .build();

        // Запрос с невалидными данными:
        invalidRequestSpecification = new RequestSpecBuilder()
                .addQueryParam("apiKey", "04fbf7a95bcc4e4199ceb3eea987551111")
                .log(LogDetail.ALL)
                .build();

        // Ответ с невалидными данными:
        invalidResponseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(401)
                .expectStatusLine("HTTP/1.1 401 Unauthorized")
                .expectContentType(ContentType.JSON)
                .expectResponseTime(Matchers.lessThan(7500L))
                .build();
    }

    // Зануление запросов и ответов:
    @AfterEach
    void afterTest(){
        RestAssured.requestSpecification = null;
        RestAssured.responseSpecification = null;
    }

    // =========================================================
    // ======== Методы для работы с параметрами запроса ========
    // =========================================================

    // Методы для работы со спецификацией запроса:
    // Метод для добавления параметра к запросу Get/Post:
    public void addRequestSpecificationParameter(RequestSpecification requestSpecification, String parameterName, String parameterValues) {
        RestAssured.requestSpecification = requestSpecification
                .queryParam(parameterName, parameterValues);
    }

    // Метод для добавления тела к запросу Post:
    public void addRequestSpecificationParameterBody(RequestSpecification requestSpecification, String parameterName, String parameterValues) {
        RestAssured.requestSpecification = requestSpecification
                .body(parameterName + "=" + parameterValues);
    }

    // Методы для добавления тела к запросу Post:
    public void addRequestSpecificationParameterBody(RequestSpecification requestSpecification, String parameterValues) {
        RestAssured.requestSpecification = requestSpecification
                .body(parameterValues);
    }

    // Альтернативный метод для добавления тела к запросу Post:
    public void addRequestSpecificationPostBodyParam(RequestSpecification requestSpecification, String date, String slot, String position, String nameIngredients) {
        RestAssured.requestSpecification = requestSpecification
                .body("{\n \"date\": " + date + ",\n" //  1654072994 - 01/06
                        + " \"slot\": " + slot + ",\n"
                        + " \"position\": " + position + ",\n"
                        + " \"type\": \"INGREDIENTS\",\n"
                        + " \"value\": {\n"
                        + " \"ingredients\": [\n"
                        + " {\n"
                        + " \"name\": \"" + nameIngredients + "\"\n"
                        + " }\n"
                        + " ]\n"
                        + " }\n"
                        + "}");
    }

    // Методы для работы со спецификацией ответа:
    // Метод для Get:
    void addResponceSpecification(ResponseSpecification responseSpecification, Boolean resultContainsData, Integer offset, Integer number, Integer totalResults){
        // Добавление данных в спецификацию ответа:
        if(resultContainsData) {
            RestAssured.responseSpecification = responseSpecification
                    .expect()
                    .body("results", not(empty()))          // результат содержит данные
                    .body("offset", equalTo(offset))        // смещение равно, начало от 0
                    .body("number", equalTo(number))        // номер равен
                    .body("totalResults", not(equalTo(totalResults)));  // итоговый результат есть и не равен 0
        } else {
            RestAssured.responseSpecification = responseSpecification
                    .expect()
                    .body("results", empty())               // результат не содержит данных
                    .body("offset", equalTo(offset))        // смещение равно, начало от 0
                    .body("number", equalTo(number))        // номер равен
                    .body("totalResults", equalTo(totalResults)); // итоговый результат отсутствует
        }
    }

    // Методы для добавения параметра к ответу:
    // - пустое тело ответа:
    public void addResponceSpecificationEmpty(ResponseSpecification responseSpecification, String parameterName){
        RestAssured.responseSpecification = responseSpecification
                .expect()
                .body(parameterName, empty());
    }

    // - тело ответа с содержимым:
    public void addResponceSpecificationNotEmpty(ResponseSpecification responseSpecification, String parameterName){
        RestAssured.responseSpecification = responseSpecification
                .expect()
                .body(parameterName, not(empty()));
    }

    // - тело ответа содержит параметры:
    public void addResponceSpecificationPost(ResponseSpecification responseSpecification, String cuisine, Float confidence) {
        RestAssured.responseSpecification = responseSpecification
                .expect()
                .body("cuisine", equalTo(cuisine))
                .body("confidence", equalTo(confidence));
    }

    // =========================================================
    // ========================= Тесты =========================
    // =========================================================

    // Тест для метода Get, невалидный токен
    @Nested
    @DisplayName("Test with invalid token for method GET")
    class getMethodWithInvalidToken {

        // Search Recipes_TC#0_Test invalid token
        @Test
        @DisplayName("Search Recipes_TC#0_Test invalid token")
        @Description("Search Recipes_TC#0_Test invalid token")
        @Link("https://api.spoonacular.com/recipes/complexSearch")
        @Issue("localhost")
        @Tag("getRecipeWithInvalidTokenPositiveTest")
        public void getRecipeWithInvalidTokenPositiveTest() {
            given()
                    .spec(invalidRequestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch")
                    .prettyPeek()
                    .then()
                    .spec(invalidResponseSpecification);
        }
    }

    // Набор тестов для метода Get
    @Nested
    @DisplayName("Test's for request method GET")
    class getTests {

        // Search Recipes_TC#1_Test header and responce data
        @Test
        @DisplayName("Search Recipes_TC#1_Test header and responce data")
        @Description("Search Recipes_TC#1_Test header and responce data")
        @Link("https://api.spoonacular.com/recipes/complexSearch")
        @Issue("localhost")
        @Tag("getRecipeWithValidTokenPositiveTest")
        public void getRecipeWithValidTokenPositiveTest() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#2_Test null data in all params
        @Test
        @DisplayName("Search Recipes_TC#2_Test null data in all params")
        @Description("Search Recipes_TC#2_Test null data in all params")
        @Link("https://api.spoonacular.com/recipes/complexSearch")
        @Issue("localhost")
        @Tag("getRecipeWithValidTokenPositiveTest")
        public void getRecipeWithValidTokenPositiveTest_() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#3_Test valid string data in query
        @Test
        @DisplayName("Search Recipes_TC#3_Test valid string data in query")
        @Description("Search Recipes_TC#3_Test valid string data in query")
        @Link("https://api.spoonacular.com/recipes/complexSearch")
        @Issue("localhost")
        @Tag("getRecipeWithValidQuery")
        public void getRecipeWithValidQuery() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?query=pasta")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#4_ Test invalid data in query = Churchkhella
        @Test
        @DisplayName("Search Recipes_TC#4_ Test invalid data in query = Churchkhella")
        @Description("Search Recipes_TC#4_ Test invalid data in query = Churchkhella")
        @Link("https://api.spoonacular.com/recipes/complexSearch")
        @Issue("localhost")
        @Tag("getRecipeWithInvalidQuery")
        public void getRecipeWithInvalidQuery() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?query=Churchkhella")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#5_ Test invalid data in query = null
        @Test
        @DisplayName("Search Recipes_TC#5_ Test invalid data in query = null")
        @Description("Search Recipes_TC#5_ Test invalid data in query = null")
        @Link("https://api.spoonacular.com/recipes/complexSearch")
        @Issue("localhost")
        @Tag("getRecipeWithNullQuery")
        public void getRecipeWithNullQuery() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?query=null")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#6_Test valid string data in cuisine
        @Test
        @DisplayName("Search Recipes_TC#6_Test valid string data in cuisine")
        @Description("Search Recipes_TC#6_Test valid string data in cuisine")
        @Link("https://api.spoonacular.com/recipes/complexSearch?cuisine=italian")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInCuisine")
        public void getRecipeWithValidDataInCuisine() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?cuisine=italian")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#7_Test invalid string data in cuisine
        @Test
        @DisplayName("Search Recipes_TC#7_Test invalid string data in cuisine")
        @Description("Search Recipes_TC#7_Test invalid string data in cuisine")
        @Link("https://api.spoonacular.com/recipes/complexSearch?cuisine=0")
        @Issue("localhost")
        @Tag("getRecipeWithInvalidNumDataInCuisine")
        public void getRecipeWithInvalidNumDataInCuisine() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?cuisine=0")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#8_Test invalid string data in cuisine=russian
        @Test
        @DisplayName("Search Recipes_TC#8_Test invalid string data in cuisine=russian")
        @Description("Search Recipes_TC#8_Test invalid string data in cuisine=russian")
        @Link("https://api.spoonacular.com/recipes/complexSearch?cuisine=russian")
        @Issue("localhost")
        @Tag("getRecipeWithInvalidStrDataInCuisine")
        public void getRecipeWithInvalidStrDataInCuisine() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?cuisine=russian")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#9_Test valid excludeCuisine
        @Test
        @DisplayName("Search Recipes_TC#9_Test valid excludeCuisine")
        @Description("Search Recipes_TC#9_Test valid excludeCuisine")
        @Link("https://api.spoonacular.com/recipes/complexSearch?cuisine=italian&excludeCuisine=greek")
        @Issue("localhost")
        @Tag("getRecipeWithValidCuisineAndExcludeCuisine")
        public void getRecipeWithValidCuisineAndExcludeCuisine() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?cuisine=italian&excludeCuisine=greek")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#10_Test invalid excludeCuisine
        @Test
        @DisplayName("Search Recipes_TC#10_Test invalid excludeCuisine")
        @Description("Search Recipes_TC#10_Test invalid excludeCuisine")
        @Link("https://api.spoonacular.com/recipes/complexSearch?cuisine=italian&excludeCuisine=italian")
        @Issue("localhost")
        @Tag("getRecipeWithInValidDataInExcludeCuisine")
        public void getRecipeWithInValidDataInExcludeCuisine() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?cuisine=italian&excludeCuisine=italian")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#11_Test valid diet
        @Test
        @DisplayName("Search Recipes_TC#11_Test valid diet")
        @Description("Search Recipes_TC#11_Test valid diet")
        @Link("https://api.spoonacular.com/recipes/complexSearch?diet=vegetarian")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInDiet")
        public void getRecipeWithValidDataInDiet() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?diet=vegetarian")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#12_Test valid intolerances
        @Test
        @DisplayName("Search Recipes_TC#12_Test valid intolerances")
        @Description("Search Recipes_TC#12_Test valid intolerances")
        @Link("https://api.spoonacular.com/recipes/complexSearch?intolerances=gluten")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInDiet")
        public void getRecipeWithValidDataIntolerances() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?intolerances=gluten")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#13_Test valid equipment
        @Test
        @DisplayName("Search Recipes_TC#13_Test valid equipment")
        @Description("Search Recipes_TC#13_Test valid equipment")
        @Link("https://api.spoonacular.com/recipes/complexSearch?equipment=blender, bowl")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInEquipment")
        public void getRecipeWithValidDataInEquipment() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?equipment=blender, bowl")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#14_Test valid includeIngredients
        @Test
        @DisplayName("Search Recipes_TC#14_Test valid includeIngredients")
        @Description("Search Recipes_TC#14_Test valid includeIngredients")
        @Link("https://api.spoonacular.com/recipes/complexSearch?includeIngredients=tomato,cheese")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInEquipment")
        public void getRecipeWithValidDataInIncludeIngredients() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?includeIngredients=tomato,cheese")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#15_Test invalid includeIngredients
        @Test
        @DisplayName("Search Recipes_TC#15_Test invalid includeIngredients")
        @Description("Search Recipes_TC#15_Test invalid includeIngredients")
        @Link("https://api.spoonacular.com/recipes/complexSearch?includeIngredients=zero")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInEquipment")
        public void getRecipeWithInvalidDataInIncludeIngredients() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?includeIngredients=zero")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#16_Test valid number=1
        @Test
        @DisplayName("Search Recipes_TC#16_Test valid number=1")
        @Description("Search Recipes_TC#16_Test valid number=1")
        @Link("https://api.spoonacular.com/recipes/complexSearch?number=1")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInEquipment")
        public void getRecipeWithValidDataInNumber1() {
            addResponceSpecification(responseSpecification,true, 0, 1, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?number=1")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#17_Test valid number=100
        @Test
        @DisplayName("Search Recipes_TC#17_Test valid number=100")
        @Description("Search Recipes_TC#17_Test valid number=100")
        @Link("https://api.spoonacular.com/recipes/complexSearch?number=100")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInNumber100")
        public void getRecipeWithValidDataInNumber100() {
            addResponceSpecification(responseSpecification,true, 0, 100, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?number=100")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#18_Test invalid number=101
        @Test
        @DisplayName("Search Recipes_TC#18_Test invalid number=101")
        @Description("Search Recipes_TC#18_Test invalid number=101")
        @Link("https://api.spoonacular.com/recipes/complexSearch?number=101")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInNumber101")
        public void getRecipeWithInvalidDataInNumber101() {
            addResponceSpecification(responseSpecification,true, 0, 100, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?number=101")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#19_Test invalid number=0
        @Test
        @DisplayName("Search Recipes_TC#19_Test invalid number=0")
        @Description("Search Recipes_TC#19_Test invalid number=0")
        @Link("https://api.spoonacular.com/recipes/complexSearch?number=0")
        @Issue("localhost")
        @Tag("getRecipeWithValidDataInNumber0")
        public void getRecipeWithInvalidDataInNumber0() {
            addResponceSpecification(responseSpecification,true, 0, 100, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?number=101")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#20_Test valid numdata CPFC
        @Test
        @DisplayName("Search Recipes_TC#20_Test valid numdata CPFC")
        @Description("Search Recipes_TC#20_Test valid numdata CPFC")
        @Link("https://api.spoonacular.com/recipes/complexSearch?minProtein=10&maxProtein=100&minCalories=50&maxCalories=800&minFat=1&maxFat=100")
        @Issue("localhost")
        @Tag("getRecipeWithValidNumDataCPFC")
        public void getRecipeWithValidNumDataCPFC() {
            addResponceSpecification(responseSpecification,true, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?minProtein=10&maxProtein=100&minCalories=50&maxCalories=800&minFat=1&maxFat=100")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#21_Test invalid numdata CPFC < 0
        @Test
        @DisplayName("Search Recipes_TC#21_Test invalid numdata CPFC < 0")
        @Description("Search Recipes_TC#21_Test invalid numdata CPFC < 0")
        @Link("https://api.spoonacular.com/recipes/complexSearch?minProtein=10&maxProtein=100&minCalories=50&maxCalories=800&minFat=1&maxFat=100")
        @Issue("localhost")
        @Tag("getRecipeWithInvalidNumDataCPFCLess100")
        public void getRecipeWithInvalidNumDataCPFCLess100() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?minCarbs=-1&maxCarbs=0&minProtein=-1&maxProtein=0&minCalories=-1&maxCalories=0&minFat=-1&maxFat=0")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Search Recipes_TC#22_Test invalid numdata CPFC > 100
        @Test
        @DisplayName("Search Recipes_TC#22_Test invalid numdata CPFC > 100")
        @Description("Search Recipes_TC#22_Test invalid numdata CPFC > 100")
        @Link("https://api.spoonacular.com/recipes/complexSearch?minCarbs=100&maxCarbs=1000&minProtein=100&maxProtein=1000&minCalories=100&maxCalories=1000&minFat=100&maxFat=1000")
        @Issue("localhost")
        @Tag("getRecipeWithInvalidNumDataCPFCMore100")
        public void getRecipeWithInvalidNumDataCPFCMore100() {
            addResponceSpecification(responseSpecification,false, 0, 10, 0);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/recipes/complexSearch?minCarbs=100&maxCarbs=1000&minProtein=100&maxProtein=1000&minCalories=100&maxCalories=1000&minFat=100&maxFat=1000")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }
    }

    // Набор тестов для метода Post
    @Nested
    @DisplayName("Test's for request method POST")
    class postTests {

        // Classify Cuisine_TC#0_Null params
        @Test
        @DisplayName("Classify Cuisine_TC#0_Null params")
        @Description("Classify Cuisine_TC#0_Null params")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithNullQuery")
        public void getCuisineWithNullQuery() {
            addResponceSpecificationPost(responseSpecification, "Mediterranean", 0.0F);

            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#1_Ttitle in params "Falafel Burgers"
        @Test
        @DisplayName("Classify Cuisine_TC#1_Ttitle in params \"Falafel Burgers\"")
        @Description("Classify Cuisine_TC#1_Ttitle in params \"Falafel Burgers\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParam_FalafelBurgers")
        public void getCuisineWithParam_FalafelBurgers() {
            addRequestSpecificationParameter(requestSpecification,"title", "Falafel Burgers");
            addResponceSpecificationPost(responseSpecification,"American", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#2_Title in body "Falafel Burgers"
        @Test
        @DisplayName("Classify Cuisine_TC#2_Title in body \"Falafel Burgers\"")
        @Description("Classify Cuisine_TC#2_Title in body \"Falafel Burgers\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("ggetCuisineWithParamInBody_FalafelBurgers")
        public void getCuisineWithParamInBody_FalafelBurgers() {
            addRequestSpecificationParameterBody(requestSpecification,"title", "Falafel Burgers");
            addResponceSpecificationPost(responseSpecification,"American", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#3_Ttitle in body "Falafel Burger"
        @Test
        @DisplayName("Classify Cuisine_TC#3_Ttitle in body \"Falafel Burger\"")
        @Description("Classify Cuisine_TC#3_Ttitle in body \"Falafel Burger\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParamInBody_FalafelBurger")
        public void getCuisineWithParamInBody_FalafelBurger() {
            addRequestSpecificationParameterBody(requestSpecification,"title", "Falafel Burger");
            addResponceSpecificationPost(responseSpecification,"Middle Eastern", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#4_Title in body "Falafel Burger" and lang in params
        @Test
        @DisplayName("Classify Cuisine_TC#4_Title in body \"Falafel Burger\" and lang in params")
        @Description("Classify Cuisine_TC#4_Title in body \"Falafel Burger\" and lang in params")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParamsInBody")
        public void getCuisineWithParamsInBody_FalafelBurger() {
            addRequestSpecificationParameterBody(requestSpecification,"title", "Falafel Burger");
            addRequestSpecificationParameter(requestSpecification,"language", "de");
            addResponceSpecificationPost(responseSpecification,"Middle Eastern", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#5_Title in params "$50,000 Burger"
        @Test
        @DisplayName("Classify Cuisine_TC#5_Title in params \"$50,000 Burger\"")
        @Description("Classify Cuisine_TC#5_Title in params \"$50,000 Burger\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParams_$50000Burger")
        public void getCuisineWithParam_$50000Burger() {
            addRequestSpecificationParameter(requestSpecification,"title", "$50,000 Burger");
            addResponceSpecificationPost(responseSpecification,"American", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#6_Title in body ''$50,000 Burger"
        @Test
        @DisplayName("Classify Cuisine_TC#6_Title in body ''$50,000 Burger\"")
        @Description("Classify Cuisine_TC#6_Title in body ''$50,000 Burger\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParams_$50000Burger")
        public void getCuisineWithParamsInBody_$50000Burger() {
            addRequestSpecificationParameterBody(requestSpecification,"title", "$50,000 Burger");
            addResponceSpecificationPost(responseSpecification,"American", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#7_Title in params "$50,000 Burger" & "lang=en"
        @Test
        @DisplayName("Classify Cuisine_TC#7_Title in params \"$50,000 Burger\" & \"lang=en\"")
        @Description("Classify Cuisine_TC#7_Title in params \"$50,000 Burger\" & \"lang=en\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParams_$50000Burger")
        public void getCuisineWithParams_$50000Burger() {
            addRequestSpecificationParameterBody(requestSpecification,"title", "$50,000 Burger");
            addRequestSpecificationParameter(requestSpecification,"language","en");
            addResponceSpecificationPost(responseSpecification,"American", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#8_Title in params "$50,000 Burger" & "lang=de"
        @Test
        @DisplayName("Classify Cuisine_TC#8_Title in params \"$50,000 Burger\" & \"lang=de\"")
        @Description("Classify Cuisine_TC#8_Title in params \"$50,000 Burger\" & \"lang=de\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParams")
        public void getCuisineWithParams() {
            addRequestSpecificationParameterBody(requestSpecification,"title", "$50,000 Burger");
            addRequestSpecificationParameter(requestSpecification,"language","de");
            addResponceSpecificationPost(responseSpecification,"American", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#9_Title in params "Amaretto Tiramisu"
        @Test
        @DisplayName("Classify Cuisine_TC#9_Title in params \"Amaretto Tiramisu\"")
        @Description("Classify Cuisine_TC#9_Title in params \"Amaretto Tiramisu\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParam_AmarettoTiramisu")
        public void getCuisineWithParam_AmarettoTiramisu() {
            addRequestSpecificationParameter(requestSpecification,"title", "Amaretto Tiramisu");
            addResponceSpecificationPost(responseSpecification,"Mediterranean", 0.85F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#10_"ingredientList=eggs"
        @Test
        @DisplayName("Classify Cuisine_TC#10_\"ingredientList=eggs\"")
        @Description("Classify Cuisine_TC#10_\"ingredientList=eggs\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParamIngredientList")
        public void getCuisineWithParamIngredientList() {
            addRequestSpecificationParameter(requestSpecification,"ingredientList", "eggs");
            addResponceSpecificationPost(responseSpecification,"Mediterranean", 0.0F);
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }

        // Classify Cuisine_TC#11_"ingredientList=3 oz pork shoulder"
        @Test
        @DisplayName("Classify Cuisine_TC#11_\"ingredientList=3 oz pork shoulder\"")
        @Description("Classify Cuisine_TC#11_\"ingredientList=3 oz pork shoulder\"")
        @Link("https://api.spoonacular.com/recipes/cuisine")
        @Issue("localhost")
        @Tag("getCuisineWithParamIngredientList2")
        public void getCuisineWithParamIngredientList2() {
            addRequestSpecificationParameter(requestSpecification,"ingredientList", "3 oz pork shoulder");
            addResponceSpecificationPost(responseSpecification,"Mediterranean", 0.0F);  //"Italian"
            given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/recipes/cuisine")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }
    }

    // Набор тестов для mealplanner:
    @Data
    @Nested
    @DisplayName("Test's for mealplanner")
    class mealplannerTests {

        // Mealplanner_TC#1_Get mealplanner empty week
        @Test
        @DisplayName("Mealplanner_TC#1_Get mealplanner empty week")
        @Description("Mealplanner_TC#1_Get mealplanner empty week")
        @Link("https://api.spoonacular.com/mealplanner/creatorpi/week/2022-05-23")
        @Issue("localhost")
        @Tag("getMealplanner")
        public void getMealplannerEmptyWeek() {
            addRequestSpecificationParameter(requestSpecification,"hash", hash);
            addResponceSpecificationEmpty(responseSpecificationAlt,"days");
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/mealplanner/creatorpi/week/2022-05-23")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecificationAlt);
        }

        // Mealplanner_TC#2_Get mealplanner not empty week
        @Test
        @DisplayName("Mealplanner_TC#2_Get mealplanner not empty week")
        @Description("Mealplanner_TC#2_Get mealplanner not empty week")
        @Link("https://api.spoonacular.com/mealplanner/creatorpi/week/2022-05-30")
        @Issue("localhost")
        @Tag("getMealplanner")
        public void getMealplannerNotEmptyWeek() {
            addRequestSpecificationParameter(requestSpecification,"hash", hash);
            addResponceSpecificationNotEmpty(responseSpecificationAlt,"days");
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/mealplanner/creatorpi/week/2022-05-30")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecificationAlt);
        }

        // Mealplanner_TC#3_Add mealplanner
        @Test
        @DisplayName("Mealplanner_TC#3_Add mealplanner with tearDown")
        @Description("Mealplanner_TC#3_Add mealplanner with tearDown")
        @Link("https://api.spoonacular.com/mealplanner/creatorpi/week/2022-06-01")
        @Issue("localhost")
        @Tag("getMealplanner")
        public void addMealplanner() {
            addRequestSpecificationParameter(requestSpecification,"hash", hash);
            addRequestSpecificationPostBodyParam(requestSpecification,"1654072994", "3", "1", "3 apple");

            // Альтернативный способ передачи body в запрос:
//            addRequestSpecificationParameterBody(requestSpecification, "{\n \"date\": 1654072994,\n" //  1654072994 - 01/06
//                    + " \"slot\": 3,\n"
//                    + " \"position\": 1,\n"
//                    + " \"type\": \"INGREDIENTS\",\n"
//                    + " \"value\": {\n"
//                    + " \"ingredients\": [\n"
//                    + " {\n"
//                    + " \"name\": \"3 apple and pineapple\"\n"
//                    + " }\n"
//                    + " ]\n"
//                    + " }\n"
//                    + "}");

            id = given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/mealplanner/creatorpi/items")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecificationAlt)
                    .extract()
                    .jsonPath()
                    .get("id")
                    .toString();

            tearDown();
        }

        public void tearDown() {
            given()
                    .spec(requestSpecification)
                    .delete("https://api.spoonacular.com/mealplanner/creatorpi/items/" + id)
                    .prettyPeek()
                    .then()
                    .spec(responseSpecificationAlt);
        }
    }

    // Набор тестов для mealplanner shopping-list:
    @Data
    @Nested
    @DisplayName("Test's for mealplanner shopping-list")
    class Tests {

        // Mealplanner_TC#1_Get shopping list
        @Test
        @DisplayName("Mealplanner-shopping-list_TC#1_Get shopping list")
        @Description("Mealplanner-shopping-list_TC#1_Get shopping list")
        @Link("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list")
        @Issue("localhost")
        @Tag("getShoppinglistWeek")
        public void getShoppinglistWeek() {
            addRequestSpecificationParameter(requestSpecification,"hash", hash);
            given()
                    .spec(requestSpecification)
                    .when()
                    .get("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecificationAlt);
        }

        // Mealplanner-shopping-list_TC#2_Add item in shopping list
        @Test
        @DisplayName("Mealplanner-shopping-list_TC#2_Add item in shopping list")
        @Description("Mealplanner-shopping-list_TC#2_Add item in shopping list")
        @Link("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list")
        @Issue("localhost")
        @Tag("addItemInShoppinglist")
        public void addItemInShoppinglist() {
            addRequestSpecificationParameter(requestSpecification,"hash", hash);
            addRequestSpecificationParameterBody(requestSpecification,"{\n \"item\": \"1 package baking powder\",\n"
                    + " \"aisle\": \"Baking\",\n"
                    + " \"parse\": true\n"
                    + "}");

            id = given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list/items")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification)
                    .extract()
                    .jsonPath()
                    .get("id")
                    .toString();

            tearDown();
        }

        public void tearDown() {
            given()
                    .spec(requestSpecification)
                    .delete("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list/items/" + id)
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification);
        }
    }

    // Тест для lombok:
    @Nested
    @DisplayName("Example test with lombok")
    class exampleLombok {
        // Mealplanner-shopping-list_TC#1_Add item in shopping list Example Lombok
        @Test
        @DisplayName("Mealplanner-shopping-list_TC#1_Add item in shopping list Example Lombok")
        @Description("Mealplanner-shopping-list_TC#1_Add item in shopping list Example Lombok")
        @Link("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list")
        @Issue("localhost")
        @Tag("addItemInShoppinglistWithLombok")
        public void addItemInShoppinglistWithLombok() {
            addRequestSpecificationParameter(requestSpecification,"hash", hash);
            addRequestSpecificationParameterBody(requestSpecification,"{\n \"item\": \"1 lemon\",\n"
                    + " \"aisle\": \"Baking\",\n"
                    + " \"parse\": true\n"
                    + "}");

            AccountInfoResponse response = given()
                    .spec(requestSpecification)
                    .when()
                    .post("https://api.spoonacular.com/mealplanner/creatorpi/shopping-list/items")
                    .prettyPeek()
                    .then()
                    .spec(responseSpecification)
                    .extract()
                    .body()
                    .as(org.example.AccountInfoResponse.class);

            assertThat(response.getName(), equalTo("lemon"));
            assertThat(response.getIngredientId(), equalTo(9150));
            assertThat(response.getCost(), equalTo(50.0));
            assertThat(response.getAisle(), equalTo("Baking"));
            assertThat(response.getMeasures().getOriginal().getAmount(), equalTo(1.0));
        }
    }
}