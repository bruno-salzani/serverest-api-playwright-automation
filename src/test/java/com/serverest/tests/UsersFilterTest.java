package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.User;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class UsersFilterTest {

    @Test
    @Description("Valida filtro combinado por nome e administrador")
    void deveFiltrarUsuariosPorNomeEAdministrador() {
        User user = User.builder()
                .nome("Bruno QA")
                .email("bruno.qa+" + System.currentTimeMillis() + "@teste.com")
                .password("teste123")
                .administrador("true")
                .build();
        AuthHelper.createUser(user);

        List<String> ids = given()
                .spec(Specs.requestSpec())
                .queryParam("nome", "Bruno QA")
                .queryParam("administrador", "true")
                .when()
                .get(Routes.USERS)
                .then()
                .statusCode(200)
                .time(lessThan(2000L))
                .extract()
                .path("usuarios._id");

        org.junit.jupiter.api.Assertions.assertFalse(ids.isEmpty());
    }

    @Test
    @Description("Valida resposta com caracteres especiais nos filtros")
    void deveAceitarCaracteresEspeciaisNoFiltro() {
        given()
                .spec(Specs.requestSpec())
                .queryParam("nome", "Bruno@QA")
                .when()
                .get(Routes.USERS)
                .then()
                .statusCode(200)
                .body("quantidade", notNullValue());
    }

    @Test
    @Description("Valida coerência do campo quantidade na listagem")
    void deveValidarQuantidadeDaListagem() {
        int quantidade = given()
                .spec(Specs.requestSpec())
                .when()
                .get("/usuarios")
                .then()
                .statusCode(200)
                .extract()
                .path("quantidade");

        List<?> usuarios = given()
                .spec(Specs.requestSpec())
                .when()
                .get("/usuarios")
                .then()
                .statusCode(200)
                .extract()
                .path("usuarios");

        org.junit.jupiter.api.Assertions.assertEquals(quantidade, usuarios.size());
    }
}
