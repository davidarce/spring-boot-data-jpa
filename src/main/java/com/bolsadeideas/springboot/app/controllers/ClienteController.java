package com.bolsadeideas.springboot.app.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.service.IClienteService;
import com.bolsadeideas.springboot.app.models.service.IUploadFileService;
import com.bolsadeideas.springboot.app.util.paginator.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	protected Log logger = LogFactory.getLog(this.getClass());
	
	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IUploadFileService uploadFileService;

	@Autowired
	private MessageSource messageSource;

	@Secured({"ROLE_USER"})
    @GetMapping(value="/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
		
		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
		
	}

    @PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping(value="/ver/{id}")
	public String ver(@PathVariable(value="id") Long id, Map<String, Object> model, RedirectAttributes flash, Locale locale) {
		
		Cliente cliente = clienteService.fetchClienteByIdWithFacturas(id);
		if(cliente == null) {
			flash.addFlashAttribute("error", messageSource.getMessage("text.cliente.flash.db.error", null, locale));
			return "redirect:/listar";
		}
		
		model.put("cliente", cliente);
		model.put("titulo",  messageSource.getMessage("text.cliente.detalle.titulo", null, locale).concat(": ").concat(cliente.getNombre()));
		
		return "ver";
	}
	
	@RequestMapping(value = {"/listar", "/"}, method=RequestMethod.GET)
	public String listar(@RequestParam(name="page", defaultValue="0") int page, Model model,
						 HttpServletRequest request,
						 Locale locale) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    if (hasRole("ROLE_ADMIN")) {
	        logger.info("Hola ".concat(authentication.getName()).concat(" tienes acceso!"));
        } else {
            logger.info("Hola ".concat(authentication.getName()).concat(" no tienes acceso!"));
        }

        SecurityContextHolderAwareRequestWrapper securityContext = new SecurityContextHolderAwareRequestWrapper(request, "");

	    if (securityContext.isUserInRole("ROLE_ADMIN")) {
            logger.info("Forma usando SecurityContextHolderAwareRequestWrapper: Hola ".concat(authentication.getName()).concat(" tienes acceso!"));
        } else {
            logger.info("Forma usando SecurityContextHolderAwareRequestWrapper: Hola ".concat(authentication.getName()).concat(" no tienes acceso!"));
        }

        if (request.isUserInRole("ROLE_ADMIN")) {
            logger.info("Forma usando HttpServletRequest: Hola ".concat(authentication.getName()).concat(" tienes acceso!"));
        } else {
            logger.info("Forma usando HttpServletRequest: Hola ".concat(authentication.getName()).concat(" no tienes acceso!"));
        }

		Pageable pageRequest = PageRequest.of(page, 4);
		
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		model.addAttribute("titulo", messageSource.getMessage("text.cliente.listar.titulo", null, locale));
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

    @Secured("ROLE_ADMIN")
	@RequestMapping(value="/form")
	public String crear(Map<String, Object> model, Locale locale) {

		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", messageSource.getMessage("text.cliente.form.titulo.crear", null, locale));
		return "form";
	}

    @Secured("ROLE_ADMIN")
	@RequestMapping(value="/form", method=RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash,
						  SessionStatus status, Locale locale) {

		if(result.hasErrors()) {
			model.addAttribute("titulo", (cliente.getId() != null) ?
					messageSource.getMessage("text.cliente.form.titulo.editar", null, locale) :
					messageSource.getMessage("text.cliente.form.titulo.crear", null, locale));
			return "form";
		}

		if(!foto.isEmpty()) {
			
			String uniqueFileName = null;
			
			if(cliente.getId() != null && cliente.getId() > 0
					&& cliente.getFoto() != null && cliente.getFoto().length() > 0) {
				uploadFileService.delete(cliente.getFoto());
			}
			
			try {
				uniqueFileName = uploadFileService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}

			flash.addFlashAttribute("info", messageSource.getMessage("text.cliente.flash.foto.subir.success", null, locale) + "'" + uniqueFileName + "'");
			cliente.setFoto(uniqueFileName);
		}

		String mensajeFlash = (cliente.getId() != null) ?
				messageSource.getMessage("text.cliente.flash.editar.success", null, locale) :
				messageSource.getMessage("text.cliente.flash.crear.success", null, locale);

		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}

    @PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/form/{id}")
	public String editar(@PathVariable(value="id") Long id, Map<String, Object> model, RedirectAttributes flash, Locale locale) {
		
		Cliente cliente = null;
		
		if(id > 0) {
			cliente = clienteService.findOne(id);
			
			if(cliente == null) {
				flash.addFlashAttribute("error",  messageSource.getMessage("text.cliente.flash.db.error", null, locale));
				return "redirect:/listar";
			}
		}else {
			flash.addFlashAttribute("error", messageSource.getMessage("text.cliente.flash.id.error", null, locale));
			return "redirect:/listar";
		}
		
		model.put("cliente", cliente);
		model.put("titulo", messageSource.getMessage("text.cliente.form.titulo.editar", null, locale));
		
		return "form";
	}

    @Secured("ROLE_ADMIN")
	@RequestMapping(value="/eliminar/{id}")
	public String eimninar(@PathVariable(value="id") Long id, RedirectAttributes flash, Locale locale) {
		if(id > 0) {
			
			Cliente cliente = clienteService.findOne(id);
			clienteService.delete(id);
			flash.addFlashAttribute("success", messageSource.getMessage("text.cliente.flash.eliminar.success", null, locale));

			if(uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", String.format(
						messageSource.getMessage("text.cliente.flash.foto.eliminar.success", null, locale), cliente.getFoto()));
			}
		}
		return "redirect:/listar";
	}

	private boolean hasRole(String role) {

		SecurityContext context = SecurityContextHolder.getContext();

		if (context == null) {
			return false;
		}

		Authentication authentication = context.getAuthentication();
		if (authentication == null) {
			return false;
		}

        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

}
