package com.example.bloco_lingua_preta

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.bloco_lingua_preta.data.ServicoComEmpresa

fun imprimirRelatorioTabela(
    context: Context,
    mes: String,
    dados: Map<String, List<ServicoComEmpresa>>,
    tipoPagamentoFiltro: String
) {
    val webView = WebView(context)
    val sb = StringBuilder()
    val style = """
        <style>
            body { font-family: sans-serif; font-size: 12px; }
            table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
            th, td { border: 1px solid #000; padding: 6px; text-align: left; }
            th { background-color: #f2f2f2; }
            .total-row { font-weight: bold; background-color: #f9f9f9; }
            h2, h3 { margin: 5px 0; }
        </style>
    """.trimIndent()

    sb.append("<html><head>$style</head><body>")
    sb.append("<h2 style='text-align: center;'>RELATÓRIO DE SERVIÇOS DETALHADO POR EMPRESA</h2>")
    sb.append("<p style='text-align: center;'>Período de Referência: $mes &nbsp;|&nbsp; Tipo de pagamento: $tipoPagamentoFiltro</p><hr>")
    var totalGeralServicos = 0
    var totalGeralBlocos = 0
    var totalGeralValor = 0.0
    dados.forEach { (nomeEmpresa, lista) ->
        val totalEmpresaBlocos = lista.sumOf { it.servico.numeroBloco }
        val totalEmpresaValor = lista.sumOf { it.servico.valor }
        sb.append("<h3>$nomeEmpresa</h3>")
        sb.append("<table><tr><th>DIA</th><th>MOTORISTA</th><th>VEÍCULO</th><th>OBSERVAÇÕES</th><th>BLOCOS</th><th>VALOR</th></tr>")
        lista.forEach { item ->
            sb.append("<tr>")
            sb.append("<td>${item.servico.data}</td>")
            sb.append("<td>${item.servico.motorista}</td>")
            sb.append("<td>${item.servico.placa}</td>")
            sb.append("<td>${item.servico.observacao}</td>")
            sb.append("<td>${item.servico.numeroBloco}</td>")
            sb.append("<td>R$ ${"%.2f".format(item.servico.valor)}</td>")
            sb.append("</tr>")
        }
        sb.append("<tr class='total-row'><td>TOTAL</td><td colspan='3'></td><td>$totalEmpresaBlocos</td><td>R$ ${"%.2f".format(totalEmpresaValor)}</td></tr>")
        sb.append("</table>")
        totalGeralServicos += lista.size
        totalGeralBlocos += totalEmpresaBlocos
        totalGeralValor += totalEmpresaValor
    }
    sb.append("<table class='total-row'><tr>")
    sb.append("<td>TOTAL CONSOLIDADO DO MÊS</td><td>$totalGeralServicos Serviços</td><td>$totalGeralBlocos Blocos Lavados</td><td>R$ ${"%.2f".format(totalGeralValor)}</td>")
    sb.append("</tr></table>")
    sb.append("</body></html>")
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = view?.createPrintDocumentAdapter("Relatorio")

            if (printAdapter != null) {
                printManager.print(
                    "Relatorio_$mes",
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .build()
                )
            }
        }
    }
    webView.loadDataWithBaseURL(null, sb.toString(), "text/HTML", "UTF-8", null)
}