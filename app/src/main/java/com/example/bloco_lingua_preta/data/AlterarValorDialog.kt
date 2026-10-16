package com.example.bloco_lingua_preta.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AlterarValorDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    valorAtual: Double
) {
    var novoValor by remember { mutableStateOf("35.00") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alterar Valor Unitário") },
        text = {
            OutlinedTextField(
                value = novoValor,
                onValueChange = { novoValor = it },
                label = { Text("Valor por Bloco (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(novoValor.toDoubleOrNull() ?: 35.0)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}