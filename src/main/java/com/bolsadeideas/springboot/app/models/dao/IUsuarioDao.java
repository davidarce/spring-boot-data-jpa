package com.bolsadeideas.springboot.app.models.dao;

import org.springframework.data.repository.CrudRepository;

import com.bolsadeideas.springboot.app.models.entity.Usuario;

import java.util.Optional;

public interface IUsuarioDao extends CrudRepository<Usuario, Long>{

	Optional<Usuario> findByUsername(String username);
}
