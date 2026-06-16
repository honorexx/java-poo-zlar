import java.util.ArrayList;
import java.util.List;

public class BancoDados {
    static List<Usuario> usuarios = new ArrayList<>();
    static List<Solicitacao> solicitacoes = new ArrayList<>();
    static List<Chamado> chamados = new ArrayList<>();

    static int proximoUsuarioId = 1;
    static int proximoSolicitacaoId = 1;
    static int proximoChamadoId = 1;

    public static void criarDadosIniciais() {
        if (!usuarios.isEmpty()) {
            return;
        }

        cadastrarUsuario("admin", "Administrador Zlar", "zlar2026", "", "747171", "");
    }

    public static Usuario cadastrarUsuario(String tipo, String nome, String email, String telefone, String senha, String servicoOuEndereco) {
        Usuario usuario = new Usuario(proximoUsuarioId, tipo, nome, email, telefone, senha, servicoOuEndereco);
        proximoUsuarioId++;
        usuarios.add(usuario);
        return usuario;
    }

    public static Usuario fazerLogin(String tipo, String email, String senha) {
        for (Usuario usuario : usuarios) {
            boolean mesmoTipo = usuario.tipo.equals(tipo);
            boolean mesmoEmail = usuario.email.equalsIgnoreCase(email);
            boolean mesmaSenha = usuario.senha.equals(senha);

            if (mesmoTipo && mesmoEmail && mesmaSenha) {
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
    }

    public static void aceitarSolicitacao(int id, Usuario prestador) {
        Solicitacao solicitacao = buscarSolicitacao(id);

        if (solicitacao != null && solicitacao.status.equals("aberta")) {
            solicitacao.status = "aceita";
            solicitacao.prestadorEmail = prestador.email;
            solicitacao.prestadorNome = prestador.nome;
        }
    }

    public static void concluirSolicitacao(int id, Usuario prestador) {
        Solicitacao solicitacao = buscarSolicitacao(id);

        if (solicitacao != null && solicitacao.prestadorEmail.equals(prestador.email)) {
            solicitacao.status = "concluida";
        }
    }

    public static void pagarSolicitacao(int id, Usuario morador) {
        Solicitacao solicitacao = buscarSolicitacao(id);

        if (solicitacao != null && solicitacao.moradorEmail.equals(morador.email) && solicitacao.status.equals("concluida")) {
            solicitacao.pagamento = "pago";
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
    }

    public static void responderChamado(int id, String status, String resposta) {
        for (Chamado chamado : chamados) {
            if (chamado.id == id) {
                chamado.status = status;
                chamado.resposta = resposta;
            }
        }
    }
}
