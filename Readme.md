#  [Spring Boot] 간단한 로그인 기능 구현 - SpringSecurity

> Spring Security를 사용하여 간단하게 회원가입과 로그인 및 로그아웃 기능을 구현해보겠습니다. Django에서 auth와 같은 기능과 유사하다고 생각이 들었습니다. 그럼 이제 간단한 설명과 함께 코드를 살펴보겠습니다.

<br>

- ### 프로젝트 환경

  - Spring Boot 2.2.6

  - Java 11

  - Gradle

  - Dependencies

    - Spring-boot-starter-Web
    - Spring-boot-starter-Data-JPA
    - Spring-boot-starter-Sequrity
    - Spring-boot-starter-thymeleaf
    - Lombok

    - `implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity5'`

      > Thymeleaf에서 Spring Security의 모듈을 사용하기 위한 의존성으로 추가해주어야 합니다.

      <br>

- ### 프로젝트 구조

  ```
  java
  └── main
      └── com
          └── hooong
              └── simpleMember
                  ├── Config
                  │   └── SecurityConfig.class
                  ├── Controller
                  │   └── MemberController.class
  				        ├── Domain
                  │   ├── Member$MemberBuilder.class
                  │   └── Member.class
                  ├── Dto
                  │   ├── MemberDto$MemberDtoBuilder.class
                  │   └── MemberDto.class
                  ├── Repository
                  │   └── MemberRepository.class
                  ├── Service
                  │   ├── MemberService.class
                  │   └── Role.class
                  └── SimpleMemberApplication.class
  resources
  └── main
  		├── application.yml
  		├── static
  		└── templates
   		├── home
  		│   └── index.html
  		└── member
   			 	├── loginForm.html
  				└── signupForm.html
  ```

  <br>

<br>

- ### Spring Security Config

  > Spring Security를 사용하기 위해서 Config파일을 작성하여 필요한 메서드들을 오버라이드를 해주어야 합니다.

  - `Config/SecurityConfig.java`

  ```java
  @Configuration
  @EnableWebSecurity
  @AllArgsConstructor
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
      private MemberService memberService;
  
      @Bean
      public PasswordEncoder passwordEncoder() {
          return new BCryptPasswordEncoder();
      }
  
      @Override
      public void configure(WebSecurity web) throws Exception {
          // 인증을 무시하기 위한 설정
          web.ignoring().antMatchers("/css/**","/js/**","/img/**","/lib/**");
      }
  
      @Override
      protected void configure(HttpSecurity http) throws Exception {
          http.authorizeRequests()
                  .antMatchers("/**").permitAll()
                  .and()
              .formLogin()     // 로그인 설정
                  .loginPage("/member/login")      // 커스텀 login 페이지를 사용
                  .defaultSuccessUrl("/")      // 로그인 성공 시 이동할 페이지
                  .permitAll()
                  .and()
              .logout()
                  .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout"))
                  .logoutSuccessUrl("/")
                  .invalidateHttpSession(true)    // 세션 초기화
                  .and()
              .exceptionHandling();
      }
  
      @Override
      protected void configure(AuthenticationManagerBuilder auth) throws Exception {
          // 로그인 처리를 하기 위한 AuthenticationManagerBuilder를 설정
          auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
      }
  }
  
  ```

  - `@Configuration` : config Bean이라는 것을 명시해주는 annotation입니다.
  - `@EnableWebSecurity` : Spring Security config를 할 클래스라는 것을 명시해줍니다.
  - `WebSecurityConfigurerAdapter`를 상속받아 필요한 메서드를 구현하여 필요한 설정을 해줍니다.
  - `PasswordEncoder` : 입력받은 비밀번호를 그대로 DB에 저장하는 것이 아니고 암호화를 해서 저장을 해주어야 합니다. 따라서 이러한 암호화를 해주는 메서드로 다른 곳에서 사용할 수 있도록 @Bean으로 등록을 해줍니다.
    - `BCryptPasswordEncoder()` : password 암호화 방법 중 한 가지입니다.
  - `configure(WebSecurity web)` : WebSecurity는 FilterChainProxy를 생성하는 필터로서 `ignoring()` 을 사용하여 Spring Security가 무시할 수 있도록 설정을 할 수 있습니다. 파일의 기준 경로는 `resources/static`이라고 합니다.
  - `configure(HttpSecurity http)` : HttpSecurity는 Http로 들어오는 요청에 대하여 보안을 구성할 수 있는 클래스로 `authorizeRequests()`, `formLogin()`, `logout()`, `exceptionHandling()`과 같은 메서드들을 이용해 로그인에 대한 설정을 해줍니다.
  - `configure(AuthenticationManagerBuilder auth)` : AuthenticationManagerBuilder는 Spring Security의 모든 인증을 관리하는 AuthenticationManager를 생성하는 클래스로 UserDetailService를 통해 유저의 정보를 memberService에서 찾아 담아줍니다. 그리고 passwordEncoder로는 앞에서 Bean으로 등록한 passwordEncoder()를 사용하겠다고 설정을 해줍니다.
  - 참고 : https://spring.io/guides/gs/securing-web/

<br><br>

- ### Domain, Repository 구현

  - `Domain/Member.java`

    ```java
    @NoArgsConstructor(access=AccessLevel.PROTECTED)
    @Getter
    @Entity
    public class Member {
    
        @Id
        @GeneratedValue
        private Long id;
        private String username;
        private String password;
    
        @Builder
        public Member(Long id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }
    }
    
    ```

    > 회원의 아이디로 사용될 `username`, 비밀번호로 사용될 `password`를 만들어줍니다.

  - `Repository/MemberRepository.java`

    ```java
    package com.hooong.simpleMember.Repository;
    
    import com.hooong.simpleMember.Domain.Member;
    import org.springframework.data.jpa.repository.JpaRepository;
    
    import java.util.Optional;
    
    public interface MemberRepository extends JpaRepository<Member, Long> {
        Optional<Member> findByusername(String username);
    }
    
    ```

    > `JpaRepository<>`를 상속받아 Repository를 만들어주고 UserDetailService에서 username으로 회원을 검색할 수 있도록 메서드를 정의해 줍니다.

    <br><br>

- ### Service 구현

  - `Service/MemberService.java`

    ```java
    @Service
    @AllArgsConstructor
    public class MemberService implements UserDetailsService {
        private MemberRepository memberRepository;
    
        // 회원가입
        @Transactional
        public Long signUp(MemberDto memberDto) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            memberDto.setPassword(passwordEncoder.encode(memberDto.getPassword()));
    
            // password를 암호화 한 뒤 dp에 저장
    
            return memberRepository.save(memberDto.toEntity()).getId();
        }
    
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            // 로그인을 하기 위해 가입된 user정보를 조회하는 메서드
            Optional<Member> memberWrapper = memberRepository.findByusername(username);
            Member member = memberWrapper.get();
    
            List<GrantedAuthority> authorities = new ArrayList<>();
    				
          	// 여기서는 간단하게 username이 'admin'이면 admin권한 부여
            if("admin".equals(username)){
                authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
            } else {
                authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));
            }
    
            // 아이디, 비밀번호, 권한리스트를 매개변수로 User를 만들어 반환해준다.
            return new User(member.getUsername(), member.getPassword(), authorities);
        }
    }
    
    ```

    - `signUp(MemberDto memberDto)` : form에서 입력받은 정보를 담은 MemberDto를 받아 password를 암호화를 해준 뒤 MemberDto를 Member객체로 변환하여 JPA를 통해 save()해줍니다.

      - `Dto/MemberDto.java`

      ```java
      @Getter @Setter
      @NoArgsConstructor
      public class MemberDto {
          private Long id;
          private String username;
          private String password;
      		
        	// Member 객체로 변환
          public Member toEntity() {
              return Member.builder()
                      .id(id)
                      .username(username)
                      .password(password)
                      .build();
          }
      
          @Builder
          public MemberDto(Long id, String username, String password) {
              this.id = id;
              this.username = username;
              this.password = password;
          }
      }
      
      ```

    - `loadUserByUsername(String username)` : Spring Security가 제공하는 로그인을 사용하기 위해 `UserDetailsService`를 구현해주어야 합니다. 로그인 form에서 입력받은 username을 가지고 DB를 찾은 뒤 있다면 권한 정보를 추가해주어 UserDetails라는 객체로 반환을 해줍니다. 

      - `Service/Role.java`

        ```java
        @AllArgsConstructor
        @Getter
        public enum Role {
            ADMIN("ROLE_ADMIN"),
            MEMBER("ROLE_MEMBER");
        
            private String value;
        }
        
        ```

        <br><br>

- ### Controller 구현

  - `Controller/MemberController.java`

    ```java
    @Controller
    @AllArgsConstructor
    public class MemberController {
        private MemberService memberService;
    
        @GetMapping("/")
        public String index() {
            return "/home/index";
        }
    
        @GetMapping("/member/signup")
        public String signupForm(Model model) {
            model.addAttribute("member",new MemberDto());
    
            return "/member/signupForm";
        }
    
        @PostMapping("/member/signup")
        public String signup(MemberDto memberDto) {
            memberService.signUp(memberDto);
    
            return "redirect:/";
        }
    
        @GetMapping("/member/login")
        public String login() {
            return "/member/loginForm";
        }
    }
    
    ```

<br><br>

- ### 템플릿 작성

  - `home/index.html`

    ```html
    <html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset="UTF-8">
        <title>Simple_member</title>
    </head>
    <body>
    <h1>Simple_Login</h1>
    <h3 sec:authorize="isAuthenticated()">반갑습니다. <span sec:authentication="name"></span>님!</h3>
    <hr>
    <a sec:authorize="isAnonymous()" th:href="@{/member/login}">로그인</a>
    <a sec:authorize="isAuthenticated()" th:href="@{/member/logout}">로그아웃</a>
    <a sec:authorize="isAnonymous()" th:href="@{/member/signup}">회원가입</a>
    </body>
    </html>
    ```

    - `sec:authorize=""` : Spring Security가 제공해주는 인증에 관한 정보를 사용하기 위한 태그입니다. 따라서 `isAnonymous()`(로그인이 이루어지지 않은 상태), `isAuthenticated()`(로그인이 이루어진 상태)의 메서드를 사용하여 현재 로그인 여부 상태를 체크할 수 있습니다.

      - 참고 : [https://www.baeldung.com/spring-security-taglibs](https://www.baeldung.com/spring-security-taglibs)

    - `sec:authentication="name"` : 현재 로그인 되어있는 사용자의 name 값을 가져옵니다.

      <br>

  - `member/loginForm.html`

    ```html
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Login</title>
    </head>
    <body>
    <h1>로그인</h1>
    <hr>
    <form action="/member/login" method="post">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
    
        <input type="text" name="username" placeholder="아이디">
        <input type="password" name="password" placeholder="비밀번호">
        <button type="submit">로그인</button>
    </form>
    </body>
    </html>
    ```

    - `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />`를 사용하여 csrf토큰을 생성할 수 있습니다.
    - form 기본 action tag가 아닌 `ht:action="@{/member/login}"`을 사용하면 csrf를 자동으로 생성하여 요청을 보내주어 위의 코드를 추가하지 않아도 된다고 합니다.

    <br>

  - `member/signupForm.html`

    ```html
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>SignUp</title>
    </head>
    <body>
    <h1>회원가입</h1>
    <hr>
    <form action="/member/signup" th:object="${member}" method="post">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
    
        <input type="text" th:field="*{username}" placeholder="아이디입력">
        <input type="password" th:field="*{password}" placeholder="비밀번호">
        <button type="submit">로그인</button>
    </form>
    </body>
    </html>
    ```

    - `th:field="*{username}"` 은 자동으로 `id="username" name="username"`을 생성해줍니다.

  <br><br>

- ### 실행 화면

  - 비로그인 상태의 index

    ![index_before_login](https://user-images.githubusercontent.com/37801041/80871202-5be01400-8ce6-11ea-8e47-2cf7f14ff44e.png)

  - 회원가입

    ![signup](https://user-images.githubusercontent.com/37801041/80871215-85993b00-8ce6-11ea-9f33-50f2b0072b18.png)

  - 로그인

    ![login](https://user-images.githubusercontent.com/37801041/80871302-c1cc9b80-8ce6-11ea-8c37-1d67c265bec9.png)

  - 기본 제공되는 로그인

    > SecurityConfig에서 loginPage()설정을 하지 않고 `/login`으로 요청을 보내면 아래와 같은 기본으로 제공되는 로그인 페이지를 볼 수 있습니다. (부트스트랩 느낌이 물씬나지만 제가 테스트로 작성한것보다는 보기 좋네요!ㅎㅎ)

    ![default_login](https://user-images.githubusercontent.com/37801041/80871307-cabd6d00-8ce6-11ea-9441-91947a10ff33.png)

  - 로그인 후 index

    ![index_after_login](https://user-images.githubusercontent.com/37801041/80871220-9053d000-8ce6-11ea-84ff-4b599dfd7be9.png)