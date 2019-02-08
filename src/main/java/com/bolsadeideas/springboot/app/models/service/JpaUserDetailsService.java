package com.bolsadeideas.springboot.app.models.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bolsadeideas.springboot.app.models.dao.IUsuarioDao;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private IUsuarioDao usuarioDao;

    private Logger logger = LoggerFactory.getLogger(JpaUserDetailsService.class);

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return usuarioDao.findByUsername(username).map(user -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            user.getRoles().forEach(role -> {
                logger.info("Role: ".concat(role.getAuthority()));
                authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
            });

            if (authorities.isEmpty()) {
                logger.error("Error en el Login: Usuario '" + username + "' no tiene roles asignados!");
                throw new UsernameNotFoundException("Error en el Login: usuario '" + username + "' no tiene roles asignados!");
            }

            return new User(user.getUsername(), user.getPassword(), user.getEnabled(), true, true, true, authorities);
        }
        ).orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " no existe en el sistema!"));
    }

}
