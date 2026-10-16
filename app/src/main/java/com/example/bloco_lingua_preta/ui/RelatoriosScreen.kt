package com.example.bloco_lingua_preta.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bloco_lingua_preta.data.Empresa
import com.example.bloco_lingua_preta.data.ServicoComEmpresa
import com.example.bloco_lingua_preta.imprimirRelatorioTabela
import java.text.Normalizer
import java.util.Locale
fun String.limpar(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return temp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .replace("\\s+".toRegex(), "")
        .lowercase(Locale.getDefault())
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosScreen(listaServicos: List<ServicoComEmpresa>, listaEmpresas: List<Empresa>) {
    val context = LocalContext.current
    val servicosProcessados = remember(listaServicos) {
        listaServicos.map { item -> Pair(item, item.servico.formaPagamento.limpar()) }
    }
    val mesesDisponiveis = remember(listaServicos) {
        listaServicos.map {
            val partes = it.servico.data.split("/")
            if (partes.size == 3) "${partes[2]}-${partes[1].padStart(2, '0')}" else ""
        }.filter { it.isNotEmpty() }.distinct().sortedDescending()
    }
    var empresaSelecionada by remember { mutableStateOf<Empresa?>(null) }
    var mesSelecionado by remember(mesesDisponiveis) { mutableStateOf(mesesDisponiveis.firstOrNull() ?: "") }
    val tiposPagamento = listOf("Todos", "Dinheiro", "Crediário", "Cartão", "Pix")
    var pagamentoSelecionado by remember { mutableStateOf("Todos") }
    var expandidoEmpresa by remember { mutableStateOf(false) }
    var expandidoMes by remember { mutableStateOf(false) }
    var expandidoPagamento by remember { mutableStateOf(false) }
    val servicosFiltrados = remember(servicosProcessados, empresaSelecionada, mesSelecionado, pagamentoSelecionado) {
        servicosProcessados.filter { (item, pgtoLimpo) ->
            val partes = item.servico.data.split("/")
            val mesServico = partes.getOrNull(1)?.padStart(2, '0') ?: ""
            val anoServico = partes.getOrNull(2) ?: ""
            val dataFormatada = "$anoServico-$mesServico"

            val fData = (dataFormatada == mesSelecionado)
            val fEmpresa = (empresaSelecionada == null || item.empresa?.id == empresaSelecionada?.id)
            val vFiltro = pagamentoSelecionado.limpar()
            val fPagamento = (pagamentoSelecionado == "Todos") || (pgtoLimpo.contains(vFiltro))

            fData && fEmpresa && fPagamento
        }.map { it.first }
    }
    val htmlContent = remember(servicosFiltrados, empresaSelecionada, mesSelecionado, pagamentoSelecionado) {
        val linhasTabela = servicosFiltrados.joinToString(separator = "") { item ->
            """
            <tr>
                <td>${item.empresa?.nomeFantasia ?: "Sem Empresa"}</td>
                <td>${item.servico.data}</td>
                <td>${item.servico.motorista} (${item.servico.placa})</td>
                <td>${item.servico.formaPagamento}</td>
                <td class="valor-servico">${item.servico.valor}</td>
            </tr>
            """
        }
        val nomeEmpresaFiltro = empresaSelecionada?.nomeFantasia ?: "Todas as Empresas"
        val mesAnoFiltro = mesSelecionado.replace("-", "/").let {
            if (it.length >= 7) "${it.takeLast(2)}/${it.take(4)}" else it
        }
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; padding: 12px; background-color: #fafafa; color: #333; margin: 0; }
                h2 { color: #1f1f1f; margin-bottom: 4px; font-size: 20px; }
                .filtros { font-size: 12px; color: #666; margin-bottom: 12px; line-height: 1.4; }
                table { width: 100%; border-collapse: collapse; margin-top: 8px; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
                th, td { padding: 8px 10px; text-align: left; font-size: 12px; border-bottom: 1px solid #eee; }
                th { background-color: #f1f3f5; color: #333; font-weight: bold; }
                tr:last-child td { border-bottom: none; }
                .total { margin-top: 10px; font-weight: bold; font-size: 13px; color: #222; }
            </style>
        </head>
        <body>
            <h2>Relatório de Serviços</h2>
            <div class="filtros">
                <b>Empresa:</b> $nomeEmpresaFiltro<br>
                <b>Mês/Ano:</b> $mesAnoFiltro<br>
                <b>Pagamento:</b> $pagamentoSelecionado </div>
            <table>
                <thead>
                    <tr>
                        <th>Empresa</th>
                        <th>Data</th>
                        <th>Motorista (Placa)</th>
                        <th>Pagamento</th>
                        <th>Valor (R$)</th>
                    </tr>
                </thead>
                <tbody id="tabela-corpo">
                    ${if (linhasTabela.isNotEmpty()) linhasTabela else "<tr><td colspan='5' style='text-align:center;'>Nenhum serviço encontrado.</td></tr>"}
                </tbody>
            </table>
            <div class="total">Total de registros: ${servicosFiltrados.size}</div>
            <div class="total" id="soma-total">Soma dos Valores: R$ 0,00</div>

            <script>
                function calcularSomaTotal() {
                    let total = 0;
                    const celulas = document.querySelectorAll('.valor-servico');
                    celulas.forEach(celula => {
                        let valorTexto = celula.textContent.replace(',', '.').trim();
                        let valorNum = parseFloat(valorTexto);
                        if (!isNaN(valorNum)) {
                            total += valorNum;
                        }
                    });
                    
                    document.getElementById('soma-total').textContent = 'Soma dos Valores: R$ ' + total.toFixed(2).replace('.', ',');
                }
                window.onload = calcularSomaTotal;
            </script>
        </body>
        </html>
        """.trimIndent()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Relatórios",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Visualização estruturada em HTML5",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = expandidoEmpresa,
            onExpandedChange = { expandidoEmpresa = !expandidoEmpresa }
        ) {
            OutlinedTextField(
                value = empresaSelecionada?.nomeFantasia ?: "Todas as Empresas",
                onValueChange = {},
                readOnly = true,
                label = { Text("Empresa") },
                shape = RoundedCornerShape(14.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoEmpresa) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
            ExposedDropdownMenu(
                expanded = expandidoEmpresa,
                onDismissRequest = { expandidoEmpresa = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas as Empresas") },
                    onClick = { empresaSelecionada = null; expandidoEmpresa = false }
                )
                listaEmpresas.forEach { empresa ->
                    DropdownMenuItem(
                        text = { Text(empresa.nomeFantasia) },
                        onClick = { empresaSelecionada = empresa; expandidoEmpresa = false }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mesesDisponiveis.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandidoMes,
                    onExpandedChange = { expandidoMes = !expandidoMes },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = mesSelecionado.replace("-", "/").let { "${it.takeLast(2)}/${it.take(4)}" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mês/Ano") },
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoMes) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoMes,
                        onDismissRequest = { expandidoMes = false }
                    ) {
                        mesesDisponiveis.forEach { mes ->
                            DropdownMenuItem(
                                text = { Text(mes.replace("-", "/").let { "${it.takeLast(2)}/${it.take(4)}" }) },
                                onClick = { mesSelecionado = mes; expandidoMes = false }
                            )
                        }
                    }
                }
            }
            ExposedDropdownMenuBox(
                expanded = expandidoPagamento,
                onExpandedChange = { expandidoPagamento = !expandidoPagamento },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = pagamentoSelecionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pagamento") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoPagamento) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandidoPagamento,
                    onDismissRequest = { expandidoPagamento = false }
                ) {
                    tiposPagamento.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = { pagamentoSelecionado = tipo; expandidoPagamento = false }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = servicosFiltrados.isNotEmpty(),
            shape = RoundedCornerShape(14.dp),
            onClick = {
                val servicosAgrupados = servicosFiltrados.groupBy { it.empresa?.nomeFantasia ?: "Sem Empresa" }
                imprimirRelatorioTabela(context, mesSelecionado, servicosAgrupados, pagamentoSelecionado)
            }
        ) {
            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Imprimir Relatório Formatado", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}