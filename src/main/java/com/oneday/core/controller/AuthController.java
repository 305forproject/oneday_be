package com.oneday.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oneday.core.dto.auth.SignUpRequest;
import com.oneday.core.dto.auth.SignUpResponse;
import com.oneday.core.dto.common.ApiResponse;
import com.oneday.core.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 컨트롤러
 * 회원가입, 로그인 등 인증 관련 API를 제공합니다.
 *
 * @author zionge2k
 * @since 2025-10-31
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원가입 및 로그인 API")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;

  @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름으로 신규 사용자를 등록합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "회원가입 성공 예시", value = """
      {
        "success": true,
        "data": {
          "id": 1,
          "email": "user@example.com",
          "name": "홍길동",
          "createdAt": "2025-10-31T10:00:00"
        }
      }
      """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "유효성 검증 실패 예시", value = """
      {
        "success": false,
        "error": {
          "code": "INVALID_INPUT_VALUE",
          "message": "이메일 형식이 올바르지 않습니다"
        }
      }
      """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "리소스 충돌 (이메일 중복)", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "이메일 중복 예시", value = """
      {
        "success": false,
        "error": {
          "code": "U002",
          "message": "이미 사용 중인 이메일입니다"
        }
      }
      """)))})
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
    log.info("회원가입 요청 - Email: {}", request.email());

    SignUpResponse response = userService.signUp(request);

    log.info("회원가입 완료 - UserId: {}", response.id());

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }
}

