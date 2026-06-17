import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BancoDados {
    static List<Usuario> usuarios = new ArrayList<>();
    static List<Solicitacao> solicitacoes = new ArrayList<>();
    static List<Chamado> chamados = new ArrayList<>();

    static int proximoUsuarioId = 1;
    static int proximoSolicitacaoId = 1;
    static int proximoChamadoId = 1;

    private static final String ARQUIVO_USUARIOS = "usuarios.dat";
    private static final String ARQUIVO_SOLICITACOES = "solicitacoes.dat";
    private static final String ARQUIVO_CHAMADOS = "chamados.dat";

    public static void carregarTudo() {
        usuarios = carregarLista(ARQUIVO_USUARIOS);
        solicitacoes = carregarLista(ARQUIVO_SOLICITACOES);
        chamados = carregarLista(ARQUIVO_CHAMADOS);

        atualizarIds();

        if (!emailExiste("zlar2026")) {
            cadastrarUsuario("admin", "Administrador Zlar", "zlar2026", "", "747171", "");
        }
    }

    public static void salvarTudo() {
        salvarLista(ARQUIVO_USUARIOS, usuarios);
        salvarLista(ARQUIVO_SOLICITACOES, solicitacoes);
        salvarLista(ARQUIVO_CHAMADOS, chamados);
    }

    private static <T> void salvarLista(String arquivo, List<T> lista) {
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            saida.writeObject(lista);
        } catch (Exception erro) {
            System.out.println("Erro ao salvar " + arquivo + ". O sistema continuará funcionando.");
        }
    }

    private static <T> List<T> carregarLista(String arquivo) {
        File file = new File(arquivo);

        if (!file.exists()) {
            System.out.println("Aviso: " + arquivo + " não encontrado. Iniciando lista vazia.");
            return new ArrayList<>();
        }

        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<T>) entrada.readObject();
        } catch (Exception erro) {
            System.out.println("Erro ao carregar " + arquivo + ". Iniciando lista vazia.");
            return new ArrayList<>();
        }
    }

    private static void atualizarIds() {
        proximoUsuarioId = 1;
        for (Usuario u : usuarios) {
            if (u.id >= proximoUsuarioId) {
                proximoUsuarioId = u.id + 1;
            }
        }

        proximoSolicitacaoId = 1;
        for (Solicitacao s : solicitacoes) {
            if (s.id >= proximoSolicitacaoId) {
                proximoSolicitacaoId = s.id + 1;
            }
        }

        proximoChamadoId = 1;
        for (Chamado c : chamados) {
            if (c.id >= proximoChamadoId) {
                proximoChamadoId = c.id + 1;
            }
        }
    }

    public static void criarDadosIniciais() {
        carregarTudo();
    }

    public static Usuario cadastrarUsuario(String tipo, String nome, String email, String telefone, String senha, String servicoOuEndereco) {
        Usuario usuario = new Usuario(proximoUsuarioId, tipo, nome, email, telefone, senha, servicoOuEndereco);
        proximoUsuarioId++;
        usuarios.add(usuario);
        salvarTudo();
        return usuario;
    }

    public static Usuario fazerLogin(String tipo, String email, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.tipo.equals(tipo) && usuario.email.equalsIgnoreCase(email) && usuario.senha.equals(senha)) {
                return usuario;
            }
        }
        return null;
    }

    public static boolean emailExiste(String email) {
        for (Usuario usuario : usuarios) {
            if (usuario.email.equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public static void criarSolicitacao(Usuario morador, String servico, String data, String endereco, String descricao) {
        Solicitacao solicitacao = new Solicitacao(proximoSolicitacaoId, morador.email, morador.nome, servico, data, endereco, descricao);
        proximoSolicitacaoId++;
        solicitacoes.add(solicitacao);
        salvarTudo();
    }

    public static void aceitarSolicitacao(int id, Usuario prestador) {
        Solicitacao solicitacao = buscarSolicitacao(id);
        if (solicitacao != null && solicitacao.status.equals("aberta")) {
            solicitacao.status = "aceita";
            solicitacao.prestadorEmail = prestador.email;
            solicitacao.prestadorNome = prestador.nome;
            salvarTudo();
        }
    }

    public static void concluirSolicitacao(int id, Usuario prestador) {
        Solicitacao solicitacao = buscarSolicitacao(id);
        if (solicitacao != null && solicitacao.prestadorEmail.equals(prestador.email)) {
            solicitacao.status = "concluida";
            salvarTudo();
        }
    }

    public static void pagarSolicitacao(int id, Usuario morador) {
        Solicitacao solicitacao = buscarSolicitacao(id);
        if (solicitacao != null && solicitacao.moradorEmail.equals(morador.email) && solicitacao.status.equals("concluida")) {
            solicitacao.pagamento = "pago";
            salvarTudo();
        }
    }

    public static Solicitacao buscarSolicitacao(int id) {
        for (Solicitacao solicitacao : solicitacoes) {
            if (solicitacao.id == id) {
                return solicitacao;
            }
        }
        return null;
    }

    public static void abrirChamado(Usuario usuario, String assunto, String descricao) {
        Chamado chamado = new Chamado(proximoChamadoId, usuario.email, usuario.nome, assunto, descricao);
        proximoChamadoId++;
        chamados.add(chamado);
        salvarTudo();
    }

    public static void responderChamado(int id, String status, String resposta) {
        for (Chamado chamado : chamados) {
            if (chamado.id == id) {
                chamado.status = status;
                chamado.resposta = resposta;
                salvarTudo();
            }
        }
    }
}