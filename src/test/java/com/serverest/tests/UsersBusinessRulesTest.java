package com.serverest.tests;

import com.serverest.config.Specs;
import com.serverest.constants.Routes;
import com.serverest.model.User;
import com.serverest.model.UserCreateResponse;
import com.serverest.utils.AuthHelper;
import com.serverest.utils.CleanupExtension;
import com.serverest.utils.DataFactory;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

@Tag("regression")
@ExtendWith(CleanupExtension.class)
public class UsersBusinessRulesTest {

    @Test
    @Description("Valida unicidade de email no cadastro de usuários")
    void deveImpedirCadastroComEmailDuplicado() {
        User user = DataFactory.createDefaultUser();
        AuthHelper.createUser(user);

        given()
                .spec(Specs.requestSpec())
                .body(user)
                .when()
                .post(Routes.USERS)
                .then()
                .statusCode(400)
                .body("message", equalTo("Este email já está sendo usado"))
                .time(lessThan(2000L));
    }

    @Test
    @Description("Valida persistência de dados após cadastro")
    void devePersistirNomeEEmailAposCadastro() {
        User user = DataFactory.createDefaultUser();
        UserCreateResponse created = AuthHelper.createUser(user);

        given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS + "/" + created.get_id())
                .then()
                .statusCode(200)
                .body("nome", equalTo(user.getNome()))
                .body("email", equalTo(user.getEmail()))
                .time(lessThan(2000L));
    }

    @Test
    @Description("Valida diferenciação de perfil admin e comum")
    void deveCadastrarUsuariosComPerfisDiferentes() {
        User admin = DataFactory.createAdminUser();
        User common = DataFactory.createDefaultUser();

        UserCreateResponse adminCreated = AuthHelper.createUser(admin);
        UserCreateResponse commonCreated = AuthHelper.createUser(common);

        given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS + "/" + adminCreated.get_id())
                .then()
                .statusCode(200)
                .body("administrador", equalTo("true"));

        given()
                .spec(Specs.requestSpec())
                .when()
                .get(Routes.USERS + "/" + commonCreated.get_id())
                .then()
                .statusCode(200)
                .body("administrador", equalTo("false"));
    }
}
