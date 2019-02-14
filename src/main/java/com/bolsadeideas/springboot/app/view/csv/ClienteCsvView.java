package com.bolsadeideas.springboot.app.view.csv;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.bolsadeideas.springboot.app.models.entity.Cliente;

@Component("listar")
public class ClienteCsvView extends AbstractView {

	public ClienteCsvView() {
		setContentType("text/csv");
	}

	@Override
	protected boolean generatesDownloadContent() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		response.setHeader("Content-Disposition", "attachment; filename=\"clients.csv\"");
		response.setContentType(getContentType());

		
		Page<Cliente> clients = (Page<Cliente>) model.get("clients");
		
		ICsvBeanWriter beanWriter = new CsvBeanWriter(response.getWriter(),  CsvPreference.STANDARD_PREFERENCE);
		
		String[] header = {"id", "nombre", "apellido", "email", "createAt"};
		beanWriter.writeHeader(header);
		
		for(Cliente cliente: clients) {
			beanWriter.write(cliente, header);
		}
		
		beanWriter.close();
	}

}
