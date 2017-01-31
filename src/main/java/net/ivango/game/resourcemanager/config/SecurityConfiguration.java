package net.ivango.game.resourcemanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static String USER_ROLE = "USER";

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("alice").password("03a04a47-2694-4289-9d2b-b0ab0b3ab391").roles(USER_ROLE);
        auth.inMemoryAuthentication().withUser("bob").password("5868f7ec-ef69-4943-a46c-7e4484b9940e").roles(USER_ROLE);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login", "/logout").permitAll()
                .antMatchers("/resources/**").hasRole(USER_ROLE)
                .and()
                .requestCache()
                .requestCache(new NullRequestCache())
                .and()
                    .logout()
                    .logoutUrl("/logout")
                .and().httpBasic();
    }

}
