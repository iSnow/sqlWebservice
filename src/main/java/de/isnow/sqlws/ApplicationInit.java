package de.isnow.sqlws;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebListener
public class ApplicationInit implements ServletContextListener {
	public ApplicationInit() {}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		try {
			ServletContext ctx = evt.getServletContext();
			String infoStr = "\n\n*===================================================================*\n";
			infoStr +=       "* Application: \t\t" + ctx.getServletContextName() + "\n";
			infoStr +=       "* Context path: \t" + ctx.getContextPath() + "\n";
			infoStr +=       "* Resource path: \t" + ctx.getResource("/") + "\n";
			infoStr +=       "* Disk path: \t\t" + ctx.getRealPath("/") + "\n";
			infoStr +=       "*===================================================================*\n";
			log.info(infoStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}