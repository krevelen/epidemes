package nl.rivm.cib.episim.tpl;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link GUITemplatesServlet}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class GUITemplatesServlet extends HttpServlet
{
	/** */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet( final HttpServletRequest request,
		final HttpServletResponse response )
		throws ServletException, IOException
	{
		request.setAttribute( "users", Arrays.asList( "a", "b" ) );
		request.getRequestDispatcher( "/index.ftl" ).forward( request,
				response );
	}
}