package com.bolsadeideas.springboot.app.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.bolsadeideas.springboot.app.models.entity.Cliente;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long> {

    @Query("select c from Cliente c left join fetch c.facturas where c.id = ?1")
    Cliente fetchByIdWithFacturas(Long id);

}
