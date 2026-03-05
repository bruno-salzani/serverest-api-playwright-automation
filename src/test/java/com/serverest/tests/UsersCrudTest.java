package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.User;
import com.serverest.model.UserCreateResponse;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import com.serverest.utils.TestDataRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

@Tag("smoke")
@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class UsersCrudTest {

    @Test
    void deveCriarUsuarioComSucesso() {
        User user = DataFactory.createDefaultUser();

        String userId = given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post(Routes.USERS)
                .then()
                .spec(Specs.responseSpec())
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .body("_id", notNullValue())
                .time(lessThan(2000L))
                .extract()
                .path("_id");
        TestDataRegistry.registerUserId(userId);
    }

    @Test
    void deveListarUsuarios() {
        given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS)
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("quantidade", notNullValue())
                .time(lessThan(2000L));
    }

    @Test
    void deveBuscarUsuarioPorId() {
        User user = DataFactory.createDefaultUser();
        UserCreateResponse created = AuthHelper.createUser(user);

        given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS + "/" + created.get_id())
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("_id", equalTo(created.get_id()))
                .time(lessThan(2000L));
    }

    @Test
    void deveAtualizarUsuario() {
        String token = AuthHelper.createAdminAndGetToken();
        User user = DataFactory.createDefaultUser();
        UserCreateResponse created = AuthHelper.createUser(user);
        User updated = User.builder()
                .nome(user.getNome() + " Atualizado")
                .email(user.getEmail())
                .password(user.getPassword())
                .administrador(user.getAdministrador())
                .build();

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .body(updated)
                .when()
                .put("/usuarios/" + created.get_id())
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("message", equalTo("Registro alterado com sucesso"))
                .time(lessThan(2000L));
    }

    @Test
    void deveRemoverUsuario() {
        String token = AuthHelper.createAdminAndGetToken();
        User user = DataFactory.createDefaultUser();
        UserCreateResponse created = AuthHelper.createUser(user);

        given()
                .spec(Specs.requestSpec())
                .header("Authorization", token)
                .when()
                .delete("/usuarios/" + created.get_id())
                .then()
                .spec(Specs.responseSpec())
                .statusCode(200)
                .body("message", equalTo("Registro excluído com sucesso"))
                .time(lessThan(2000L));
    }
}
