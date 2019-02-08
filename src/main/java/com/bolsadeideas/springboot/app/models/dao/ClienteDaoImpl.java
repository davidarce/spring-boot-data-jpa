package com.bolsadeideas.springboot.app.models.dao;

import org.springframework.stereotype.Repository;

@Repository
public class ClienteDaoImpl {

//	@PersistenceContext
//	private EntityManager em;
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public List<Cliente> findAll() {
//		return em.createQuery("from Cliente").getResultList();
//	}
//
//	@Override
//	public Cliente findOne(Long id) {
//		return em.find(Cliente.class, id);
//	}
//
//	@Override
//	public void save(Cliente cliente) {
//		if (cliente.getId() != null && cliente.getId() > 0) {
//			em.merge(cliente);
//		}else {
//			em.persist(cliente);
//		}
//	}
//
//	
//	@Override
//	public void delete(Long id) {
//		em.remove(findOne(id));
//	}

}
