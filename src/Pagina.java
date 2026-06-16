import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Pagina {
    public static String montar(String titulo, String arquivo, Usuario usuario, String[][] valores) throws Exception {
        String conteudo = removerPreviaDireta(ler("templates/" + arquivo));

        if (valores != null) {
            for (String[] item : valores) {
                conteudo = conteudo.replace("{{" + item[0] + "}}", item[1]);
            }
        }

        String menu = menuPublico();
        if (usuario != null) {
            menu = menuLogado(usuario);
        }

        return ler("templates/layout.html")
                .replace("{{titulo}}", escapar(titulo))
                .replace("{{menu}}", menu)
                .replace("{{conteudo}}", conteudo);
    }

    public static String ler(String caminho) throws Exception {
        return Files.readString(Path.of(caminho), StandardCharsets.UTF_8);
    }

    static String removerPreviaDireta(String texto) {
        texto = removerBloco(texto, "<!-- direto-inicio -->", "<!-- direto-fim -->");
        texto = removerBloco(texto, "<!-- direto-rodape-inicio -->", "<!-- direto-rodape-fim -->");
        return texto;
    }

    static String removerBloco(String texto, String inicio, String fim) {
        int posInicio = texto.indexOf(inicio);
        int posFim = texto.indexOf(fim);

        if (posInicio == -1 || posFim == -1 || posFim < posInicio) {
            return texto;
        }

        return texto.substring(0, posInicio) + texto.substring(posFim + fim.length());
    }

    public static String alerta(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            return "";
        }

        return "<p class=\"alert show alert-error\">" + escapar(mensagem) + "</p>";
    }

    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    static String menuPublico() {
        return """
                <a href="/">Inicio</a>
                <a href="/login?tipo=morador">Morador</a>
                <a href="/login?tipo=prestador">Prestador</a>
                <a href="/login?tipo=admin">Admin</a>
                """;
    }

    static String menuLogado(Usuario usuario) {
        String admin = "";
        if (usuario.tipo.equals("admin")) {
            admin = """
                    <a href="/admin/usuarios">Usuarios</a>
                    <a href="/admin/suporte">Suporte admin</a>
                    """;
        }

        return """
                <a href="/painel">Painel</a>
                <a href="/suporte">Suporte</a>
                %s
                <a href="/sair">Sair</a>
                """.formatted(admin);
    }
}
