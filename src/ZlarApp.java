public class ZlarApp {
    public static void main(String[] args) throws Exception {
        BancoDados.criarDadosIniciais();
        int porta = Integer.parseInt(System.getProperty("PORT", "8090"));
        ServidorWeb servidor = new ServidorWeb(porta);
        servidor.iniciar();
    }
}
