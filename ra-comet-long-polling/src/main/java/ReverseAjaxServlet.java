import java.io.IOException;
import java.util.Date;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ReverseAjaxServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	final Queue<AsyncContext> asyncContexts = new ConcurrentLinkedQueue<>();

	final ObjectMapper mapper = new ObjectMapper();

	final Random random = new Random();

	private final Thread generator = new Thread("Event generator") {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(random.nextInt(5000));
					while (!asyncContexts.isEmpty()) {
						AsyncContext asyncContext = asyncContexts.poll();
						HttpServletResponse peer = (HttpServletResponse) asyncContext.getResponse();
						peer.getWriter().write(mapper.writeValueAsString(new String[] { "At " + new Date() }));
						peer.setStatus(HttpServletResponse.SC_OK);
						peer.setContentType("application/json");
						asyncContext.complete();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	};

	@Override
	public void init() throws ServletException {
		generator.start();
	}

	@Override
	public void destroy() {
		generator.interrupt();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		AsyncContext asyncContext = req.startAsync();
		asyncContext.setTimeout(0);
		asyncContexts.offer(asyncContext);
	}
}
