package com.example.bloco_lingua_preta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExclusaoDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var mes by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir serviços do mês") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = mes, onValueChange = { mes = it }, label = { Text("Mês (05)") })
                OutlinedTextField(value = ano, onValueChange = { ano = it }, label = { Text("Ano (2026)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(mes, ano); onDismiss() }) {
                Text("EXCLUIR", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}