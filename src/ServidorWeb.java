import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServidorWeb {
    int porta;
    HttpServer servidor;
    Map<String, Usuario> sessoes = new HashMap<>();

    public ServidorWeb(int porta) {
        this.porta = porta;
    }

    public void iniciar() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/", this::receber);
        servidor.start();

        System.out.println("Zlar aberto na web:");
        System.out.println("http://localhost:" + porta);
        System.out.println("Admin: zlar2026 / 747171");
    }

    void receber(HttpExchange troca) {
        try {
            String rota = troca.getRequestURI().getPath();
            String metodo = troca.getRequestMethod();

            if (rota.equals("/estilo.css")) {
                css(troca);
            } else if (metodo.equals("GET") && rota.equals("/")) {
                inicio(troca);
            } else if (metodo.equals("GET") && rota.equals("/login")) {
                login(troca, "");
            } else if (metodo.equals("POST") && rota.equals("/login")) {
                entrar(troca);
            } else if (metodo.equals("GET") && rota.equals("/cadastro")) {
                cadastro(troca, "");
            } else if (metodo.equals("POST") && rota.equals("/cadastro")) {
                salvarCadastro(troca);
            } else if (metodo.equals("GET") && rota.equals("/painel")) {
                painel(troca);
            } else if (metodo.equals("GET") && rota.equals("/solicitar")) {
                solicitar(troca);
            } else if (metodo.equals("POST") && rota.equals("/solicitar")) {
                salvarSolicitacao(troca);
            } else if (metodo.equals("POST") && rota.equals("/aceitar")) {
                aceitar(troca);
            } else if (metodo.equals("POST") && rota.equals("/concluir")) {
                concluir(troca);
            } else if (metodo.equals("POST") && rota.equals("/pagar")) {
                pagar(troca);
            } else if (metodo.equals("GET") && rota.equals("/suporte")) {
                suporte(troca, "");
            } else if (metodo.equals("POST") && rota.equals("/suporte")) {
                salvarSuporte(troca);
            } else if (metodo.equals("GET") && rota.equals("/admin/usuarios")) {
                adminUsuarios(troca);
            } else if (metodo.equals("GET") && rota.equals("/admin/suporte")) {
                adminSuporte(troca);
            } else if (metodo.equals("POST") && rota.equals("/admin/suporte")) {
                responderSuporte(troca);
            } else if (metodo.equals("GET") && rota.equals("/sair")) {
                sair(troca);
            } else {
                enviar(troca, 404, "Pagina nao encontrada.");
            }
        } catch (Exception erro) {
            try {
                enviar(troca, 500, "Erro: " + erro.getMessage());
            } catch (Exception ignorado) {
            }
        }
    }

    void inicio(HttpExchange troca) throws Exception {
        enviar(troca, 200, Pagina.montar("Inicio", "inicio.html", null, null));
    }

    void login(HttpExchange troca, String mensagem) throws Exception {
        String tipo = valor(query(troca).get("tipo"), "morador");
        String cadastro = "";

        if (!tipo.equals("admin")) {
            cadastro = "<a href=\"/cadastro?tipo=" + tipo + "\">Criar conta</a>";
        }

        enviar(troca, 200, Pagina.montar("Login", "login.html", null, new String[][]{
                {"tipo", Pagina.escapar(tipo)},
                {"senha", tipo.equals("admin") ? "Codigo" : "Senha"},
                {"alerta", Pagina.alerta(mensagem)},
                {"cadastro", cadastro}
        }));
    }

    void entrar(HttpExchange troca) throws Exception {
        Map<String, String> form = formulario(troca);
        Usuario usuario = BancoDados.fazerLogin(form.get("tipo"), form.get("email"), form.get("senha"));

        if (usuario == null) {
            login(troca, "Login invalido.");
            return;
        }

        String sessao = UUID.randomUUID().toString();
        sessoes.put(sessao, usuario);
        troca.getResponseHeaders().add("Set-Cookie", "sessao=" + sessao + "; Path=/");
        redirecionar(troca, "/painel");
    }

    void cadastro(HttpExchange troca, String mensagem) throws Exception {
        String tipo = valor(query(troca).get("tipo"), "morador");
        String campo = tipo.equals("prestador") ? "Servico" : "Endereco";

        enviar(troca, 200, Pagina.montar("Cadastro", "cadastro.html", null, new String[][]{
                {"tipo", Pagina.escapar(tipo)},
                {"campo", campo},
                {"alerta", Pagina.alerta(mensagem)}
        }));
    }

    void salvarCadastro(HttpExchange troca) throws Exception {
        Map<String, String> form = formulario(troca);

        if (BancoDados.emailExiste(form.get("email"))) {
            cadastro(troca, "Este e-mail ja existe.");
            return;
        }

        BancoDados.cadastrarUsuario(
                form.get("tipo"),
                form.get("nome"),
                form.get("email"),
                form.get("telefone"),
                form.get("senha"),
                form.get("servicoOuEndereco")
        );

        redirecionar(troca, "/login?tipo=" + form.get("tipo"));
    }

    void painel(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario == null) {
            redirecionar(troca, "/");
            return;
        }

        if (usuario.tipo.equals("admin")) {
            painelAdmin(troca, usuario);
        } else if (usuario.tipo.equals("morador")) {
            painelMorador(troca, usuario);
        } else {
            painelPrestador(troca, usuario);
        }
    }

    void painelAdmin(HttpExchange troca, Usuario usuario) throws Exception {
        enviar(troca, 200, Pagina.montar("Painel admin", "painel-admin.html", usuario, new String[][]{
                {"moradores", String.valueOf(contarUsuarios("morador"))},
                {"prestadores", String.valueOf(contarUsuarios("prestador"))},
                {"solicitacoes", String.valueOf(BancoDados.solicitacoes.size())},
                {"chamados", String.valueOf(BancoDados.chamados.size())}
        }));
    }

    void painelMorador(HttpExchange troca, Usuario usuario) throws Exception {
        enviar(troca, 200, Pagina.montar("Painel morador", "painel-morador.html", usuario, new String[][]{
                {"nome", Pagina.escapar(usuario.nome)},
                {"lista", solicitacoesMorador(usuario)}
        }));
    }

    void painelPrestador(HttpExchange troca, Usuario usuario) throws Exception {
        enviar(troca, 200, Pagina.montar("Painel prestador", "painel-prestador.html", usuario, new String[][]{
                {"nome", Pagina.escapar(usuario.nome)},
                {"servico", Pagina.escapar(usuario.servicoOuEndereco)},
                {"lista", solicitacoesPrestador(usuario)}
        }));
    }

    void solicitar(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario == null || !usuario.tipo.equals("morador")) {
            redirecionar(troca, "/");
            return;
        }

        enviar(troca, 200, Pagina.montar("Solicitar", "solicitar.html", usuario, new String[][]{
                {"data", LocalDate.now().toString()},
                {"endereco", Pagina.escapar(usuario.servicoOuEndereco)}
        }));
    }

    void salvarSolicitacao(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario == null || !usuario.tipo.equals("morador")) {
            redirecionar(troca, "/");
            return;
        }

        Map<String, String> form = formulario(troca);
        BancoDados.criarSolicitacao(usuario, form.get("servico"), form.get("data"), form.get("endereco"), form.get("descricao"));
        redirecionar(troca, "/painel");
    }

    void aceitar(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario != null && usuario.tipo.equals("prestador")) {
            BancoDados.aceitarSolicitacao(numero(formulario(troca).get("id")), usuario);
        }

        redirecionar(troca, "/painel");
    }

    void concluir(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario != null && usuario.tipo.equals("prestador")) {
            BancoDados.concluirSolicitacao(numero(formulario(troca).get("id")), usuario);
        }

        redirecionar(troca, "/painel");
    }

    void pagar(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario != null && usuario.tipo.equals("morador")) {
            BancoDados.pagarSolicitacao(numero(formulario(troca).get("id")), usuario);
        }

        redirecionar(troca, "/painel");
    }

    void suporte(HttpExchange troca, String mensagem) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario == null) {
            redirecionar(troca, "/");
            return;
        }

        enviar(troca, 200, Pagina.montar("Suporte", "suporte.html", usuario, new String[][]{
                {"alerta", Pagina.alerta(mensagem)},
                {"lista", Pagina.escapar(chamadosUsuario(usuario))}
        }));
    }

    void salvarSuporte(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);
        Map<String, String> form = formulario(troca);
        BancoDados.abrirChamado(usuario, form.get("assunto"), form.get("descricao"));
        suporte(troca, "Chamado aberto com sucesso.");
    }

    void adminUsuarios(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario == null || !usuario.tipo.equals("admin")) {
            redirecionar(troca, "/");
            return;
        }

        enviar(troca, 200, Pagina.montar("Usuarios", "admin-usuarios.html", usuario, new String[][]{
                {"lista", Pagina.escapar(usuarios())}
        }));
    }

    void adminSuporte(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario == null || !usuario.tipo.equals("admin")) {
            redirecionar(troca, "/");
            return;
        }

        enviar(troca, 200, Pagina.montar("Suporte admin", "admin-suporte.html", usuario, new String[][]{
                {"lista", Pagina.escapar(chamadosAdmin())}
        }));
    }

    void responderSuporte(HttpExchange troca) throws Exception {
        Usuario usuario = usuarioLogado(troca);

        if (usuario != null && usuario.tipo.equals("admin")) {
            Map<String, String> form = formulario(troca);
            BancoDados.responderChamado(numero(form.get("id")), form.get("status"), form.get("resposta"));
        }

        redirecionar(troca, "/admin/suporte");
    }

    void sair(HttpExchange troca) throws Exception {
        troca.getResponseHeaders().add("Set-Cookie", "sessao=; Path=/; Max-Age=0");
        redirecionar(troca, "/");
    }

    int contarUsuarios(String tipo) {
        int total = 0;

        for (Usuario usuario : BancoDados.usuarios) {
            if (usuario.tipo.equals(tipo)) {
                total++;
            }
        }

        return total;
    }

    String usuarios() {
        String texto = "";

        for (Usuario usuario : BancoDados.usuarios) {
            texto += usuario.resumo() + "\n";
        }

        return texto.isBlank() ? "Nenhum usuario." : texto;
    }

    String solicitacoesMorador(Usuario usuario) {
        String texto = "";

        for (Solicitacao solicitacao : BancoDados.solicitacoes) {
            if (solicitacao.moradorEmail.equals(usuario.email)) {
                texto += cardMorador(solicitacao);
            }
        }

        return texto.isBlank() ? "<p class=\"data-box\">Nenhuma solicitacao.</p>" : texto;
    }

    String solicitacoesPrestador(Usuario usuario) {
        String texto = "";

        for (Solicitacao solicitacao : BancoDados.solicitacoes) {
            boolean mesmoServico = solicitacao.servico.equalsIgnoreCase(usuario.servicoOuEndereco);
            boolean livre = solicitacao.prestadorEmail.isBlank();
            boolean minha = solicitacao.prestadorEmail.equals(usuario.email);

            if (mesmoServico && (livre || minha)) {
                texto += cardPrestador(solicitacao, minha);
            }
        }

        return texto.isBlank() ? "<p class=\"data-box\">Nenhuma solicitacao para seu servico.</p>" : texto;
    }

    String cardMorador(Solicitacao solicitacao) {
        String botaoPagar = "";

        if (solicitacao.status.equals("concluida") && solicitacao.pagamento.equals("pendente")) {
            botaoPagar = """
                    <form method="post" action="/pagar" class="inline-form">
                      <input type="hidden" name="id" value="%d">
                      <button class="btn btn-primary" type="submit">Pagar</button>
                    </form>
                    """.formatted(solicitacao.id);
        }

        return """
                <div class="data-box item-card">
                  <strong>Solicitacao #%d</strong><br>
                  Servico: %s<br>
                  Data: %s<br>
                  Endereco: %s<br>
                  Descricao: %s<br>
                  Status: %s<br>
                  Prestador: %s<br>
                  Pagamento: %s
                  %s
                </div>
                """.formatted(
                solicitacao.id,
                Pagina.escapar(solicitacao.servico),
                Pagina.escapar(solicitacao.data),
                Pagina.escapar(solicitacao.endereco),
                Pagina.escapar(solicitacao.descricao),
                Pagina.escapar(solicitacao.status),
                Pagina.escapar(solicitacao.prestadorNome.isBlank() ? "ainda nao aceitou" : solicitacao.prestadorNome),
                Pagina.escapar(solicitacao.pagamento),
                botaoPagar
        );
    }

    String cardPrestador(Solicitacao solicitacao, boolean minha) {
        String botoes = "";

        if (solicitacao.status.equals("aberta")) {
            botoes = """
                    <form method="post" action="/aceitar" class="inline-form">
                      <input type="hidden" name="id" value="%d">
                      <button class="btn btn-primary" type="submit">Aceitar</button>
                    </form>
                    """.formatted(solicitacao.id);
        } else if (minha && solicitacao.status.equals("aceita")) {
            botoes = """
                    <form method="post" action="/concluir" class="inline-form">
                      <input type="hidden" name="id" value="%d">
                      <button class="btn btn-primary" type="submit">Concluir</button>
                    </form>
                    """.formatted(solicitacao.id);
        }

        return """
                <div class="data-box item-card">
                  <strong>Solicitacao #%d</strong><br>
                  Morador: %s<br>
                  Data: %s<br>
                  Endereco: %s<br>
                  Descricao: %s<br>
                  Status: %s<br>
                  Pagamento: %s
                  %s
                </div>
                """.formatted(
                solicitacao.id,
                Pagina.escapar(solicitacao.moradorNome),
                Pagina.escapar(solicitacao.data),
                Pagina.escapar(solicitacao.endereco),
                Pagina.escapar(solicitacao.descricao),
                Pagina.escapar(solicitacao.status),
                Pagina.escapar(solicitacao.pagamento),
                botoes
        );
    }

    String chamadosUsuario(Usuario usuario) {
        String texto = "";

        for (Chamado chamado : BancoDados.chamados) {
            if (chamado.usuarioEmail.equals(usuario.email)) {
                texto += chamado.resumo() + "\n";
            }
        }

        return texto.isBlank() ? "Nenhum chamado." : texto;
    }

    String chamadosAdmin() {
        String texto = "";

        for (Chamado chamado : BancoDados.chamados) {
            texto += chamado.resumo() + "\n";
        }

        return texto.isBlank() ? "Nenhum chamado." : texto;
    }

    Usuario usuarioLogado(HttpExchange troca) {
        String cookie = troca.getRequestHeaders().getFirst("Cookie");

        if (cookie == null) {
            return null;
        }

        for (String parte : cookie.split(";")) {
            parte = parte.trim();

            if (parte.startsWith("sessao=")) {
                return sessoes.get(parte.substring(7));
            }
        }

        return null;
    }

    Map<String, String> formulario(HttpExchange troca) throws Exception {
        String corpo = new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return pares(corpo);
    }

    Map<String, String> query(HttpExchange troca) {
        return pares(valor(troca.getRequestURI().getRawQuery(), ""));
    }

    Map<String, String> pares(String texto) {
        Map<String, String> mapa = new HashMap<>();

        if (texto == null || texto.isBlank()) {
            return mapa;
        }

        for (String item : texto.split("&")) {
            String[] partes = item.split("=", 2);
            String chave = decodificar(partes[0]);
            String valor = partes.length > 1 ? decodificar(partes[1]) : "";
            mapa.put(chave, valor);
        }

        return mapa;
    }

    String decodificar(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }

    String valor(String texto, String padrao) {
        if (texto == null || texto.isBlank()) {
            return padrao;
        }

        return texto;
    }

    int numero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (Exception erro) {
            return -1;
        }
    }

    void css(HttpExchange troca) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("web/estilo.css"));
        troca.getResponseHeaders().set("Content-Type", "text/css; charset=utf-8");
        troca.sendResponseHeaders(200, bytes.length);

        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    void enviar(HttpExchange troca, int status, String texto) throws Exception {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        troca.sendResponseHeaders(status, bytes.length);

        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    void redirecionar(HttpExchange troca, String destino) throws Exception {
        troca.getResponseHeaders().set("Location", destino);
        troca.sendResponseHeaders(302, -1);
        troca.close();
    }
}
