package com.example.bloco_lingua_preta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bloco_lingua_preta.data.Empresa
import com.example.bloco_lingua_preta.data.ServicoComEmpresa
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicoFormDialog(
    servicoExistente: ServicoComEmpresa? = null,
    dataPadrao: String,
    listaEmpresas: List<Empresa>,
    valorUnitario: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Int, Int, Boolean, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var numBloco by remember { mutableStateOf(servicoExistente?.servico?.numeroBloco?.toString() ?: "") }
    var placa by remember { mutableStateOf(servicoExistente?.servico?.placa ?: "") }
    var motorista by remember { mutableStateOf(servicoExistente?.servico?.motorista ?: "") }
    var obs by remember { mutableStateOf(servicoExistente?.servico?.observacao ?: "") }
    var data by remember { mutableStateOf(servicoExistente?.servico?.data ?: dataPadrao) }
    var empresaSelecionada by remember { mutableStateOf(servicoExistente?.empresa) }
    var formaPgtoSelecionada by remember { mutableStateOf(servicoExistente?.servico?.formaPagamento?.trim() ?: "Selecionar Pagamento") }
    var expandedEmpresa by remember { mutableStateOf(false) }
    var expandedPagamento by remember { mutableStateOf(false) }
    val formasPagamento = listOf("Dinheiro","Crediário","Cartão","Pix")
    val isFormValid = numBloco.isNotBlank() && placa.isNotBlank() && motorista.isNotBlank() && empresaSelecionada != null && data.isNotBlank()
    val qtdBlocos = numBloco.toIntOrNull() ?: 0
    val valorTotal = qtdBlocos * valorUnitario

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (servicoExistente == null) "Registrar Serviço *" else "Editar Serviço *") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expandedEmpresa, onExpandedChange = { expandedEmpresa = !expandedEmpresa }) {
                    OutlinedTextField(
                        value = empresaSelecionada?.nomeFantasia ?: "", onValueChange = {}, readOnly = true,
                        label = { Text("Empresa *") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEmpresa) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedEmpresa, onDismissRequest = { expandedEmpresa = false }) {
                        listaEmpresas.forEach { emp ->
                            DropdownMenuItem(text = { Text(emp.nomeFantasia) }, onClick = { empresaSelecionada = emp; expandedEmpresa = false })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = expandedPagamento, onExpandedChange = { expandedPagamento = !expandedPagamento }) {
                    OutlinedTextField(
                        value = formaPgtoSelecionada, onValueChange = {}, readOnly = true,
                        label = { Text("Forma de Pagamento *") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPagamento) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedPagamento, onDismissRequest = { expandedPagamento = false }) {
                        formasPagamento.forEach { forma ->
                            DropdownMenuItem(text = { Text(forma) }, onClick = { formaPgtoSelecionada = forma; expandedPagamento = false })
                        }
                    }
                }
                OutlinedTextField(value = numBloco, onValueChange = { if (it.all { c -> c.isDigit() }) numBloco = it }, label = { Text("Nº de Blocos *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "R$ ${"%.2f".format(Locale.US, valorTotal)}", onValueChange = {}, label = { Text("Valor Total (R$)") }, enabled = false, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = placa, onValueChange = { placa = it }, label = { Text("Placa *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = motorista, onValueChange = { motorista = it }, label = { Text("Motorista *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = data, onValueChange = { data = it }, label = { Text("Data (dd/mm/aaaa) *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = obs, onValueChange = { obs = it }, label = { Text("Observações") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = isFormValid, onClick = { onConfirm(numBloco, placa, motorista, String.format(Locale.US, "%.2f", valorTotal), data, obs, empresaSelecionada?.id ?: 0, servicoExistente?.servico?.id ?: 0, true, formaPgtoSelecionada.trim()) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar e Imprimir") }
                TextButton(enabled = isFormValid, onClick = { onConfirm(numBloco, placa, motorista, String.format(Locale.US, "%.2f", valorTotal), data, obs, empresaSelecionada?.id ?: 0, servicoExistente?.servico?.id ?: 0, false, formaPgtoSelecionada.trim()) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar Apenas") }
                if (onDelete != null) { HorizontalDivider(); TextButton(onClick = { onDelete() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Excluir Serviço") } }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}