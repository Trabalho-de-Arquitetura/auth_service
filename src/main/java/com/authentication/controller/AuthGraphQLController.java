package com.authentication.controller;

import com.authentication.dto.*;
import com.authentication.dto.input.LoginInput;
import com.authentication.dto.input.RegistrationInput;
import com.authentication.dto.input.UserOutput;
import com.authentication.dto.input.UserRoleInput; // Importar seu UserRoleInput
import com.authentication.dto.input.ChangePasswordInput;
import com.authentication.dto.JwtTokenProvider;
import graphql.GraphQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthGraphQLController {

    private final HttpGraphQlClient userClient;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthGraphQLController(@Qualifier("userGraphQlClient") HttpGraphQlClient userClient,
                                 JwtTokenProvider tokenProvider,
                                 PasswordEncoder passwordEncoder) {
        this.userClient = userClient;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @MutationMapping
    public Mono<UserOutput> register(@Argument RegistrationInput input) {
        String createUserMutation = """
            mutation CreateUser($input: CreateUserInput!) {
                createUser(input: $input) {
                    id
                    email
                    name
                    affiliatedSchool
                    role
                }
            }
        """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of(
                "name", input.getName(),
                "email", input.getEmail(),
                "affiliatedSchool", input.getAffiliatedSchool(),
                "password", input.getPassword(),
                // enviar a string da role ("admin", "professor", "student")
                "role", input.getRole().getRole()
        ));

        return userClient.document(createUserMutation)
                .variables(variables)
                .retrieve("createUser")
                .toEntity(UserOutput.class)
                .doOnError(throwable -> {
                    throw new GraphQLException("Erro ao registrar usuário: " + throwable.getMessage(), throwable);
                });
    }

    @MutationMapping
    public Mono<AuthResponse> login(@Argument LoginInput input) {
        String getUserByEmailQuery = """
            query UserByEmail($email: String!) {
                userByEmail(email: $email) {
                    id
                    email
                    password
                    name
                    affiliatedSchool
                    role
                }
            }
        """;

        return userClient.document(getUserByEmailQuery)
                .variable("email", input.getEmail())
                .retrieve("userByEmail")
                .toEntity(UserFromUserService.class)
                .flatMap(userFromService -> {
                    if (userFromService == null || userFromService.getPassword() == null) {
                        return Mono.error(new GraphQLException("Credenciais inválidas: usuário não encontrado."));
                    }
                    if (!passwordEncoder.matches(input.getPassword(), userFromService.getPassword())) {
                        return Mono.error(new GraphQLException("Credenciais inválidas: senha incorreta."));
                    }

                    // Converter string role para UserRoleInput
                    UserRoleInput roleInput = userFromService.getRole();

                    String token = tokenProvider.generateToken(
                            userFromService.getId(),
                            userFromService.getEmail(),
                            userFromService.getName(),
                            roleInput
                    );

                    UserOutput userOutput = new UserOutput();
                    userOutput.setId(userFromService.getId());
                    userOutput.setName(userFromService.getName());
                    userOutput.setEmail(userFromService.getEmail());
                    userOutput.setAffiliatedSchool(userFromService.getAffiliatedSchool());
                    // setar a string da role para saída
                    userOutput.setRole(roleInput.getRole());

                    return Mono.just(new AuthResponse(token, userOutput));
                })
                .switchIfEmpty(Mono.error(new GraphQLException("Credenciais inválidas: usuário não encontrado.")));
    }

    @MutationMapping
    public Mono<Boolean> changePassword(@Argument ChangePasswordInput input) {
        String getUserByEmailQuery = """
            query UserByEmail($email: String!) {
                userByEmail(email: $email) {
                    id
                    password
                }
            }
        """;

        return userClient.document(getUserByEmailQuery)
                .variable("email", input.getEmail())
                .retrieve("userByEmail")
                .toEntity(UserFromUserService.class)
                .flatMap(user -> {
                    if (user == null || user.getPassword() == null) {
                        return Mono.error(new GraphQLException("Usuário não encontrado."));
                    }
                    if (!passwordEncoder.matches(input.getCurrentPassword(), user.getPassword())) {
                        return Mono.error(new GraphQLException("Senha antiga incorreta."));
                    }

                    String newPasswordHash = passwordEncoder.encode(input.getNewPassword());
                    String updateUserPasswordMutation = """
                        mutation UpdatePassword($email: String!, $newPasswordHash: String!) {
                            updateUserPassword(email: $email, newPasswordHash: $newPasswordHash)
                        }
                    """;

                    return userClient.document(updateUserPasswordMutation)
                            .variable("email", input.getEmail())
                            .variable("newPasswordHash", newPasswordHash)
                            .execute()
                            .map(response -> {
                                Boolean result = response.field("updateUserPassword").toEntity(Boolean.class);
                                return result != null && result;
                            })
                            .defaultIfEmpty(false);
                })
                .switchIfEmpty(Mono.error(new GraphQLException("Usuário não encontrado para mudança de senha.")));
    }

}
