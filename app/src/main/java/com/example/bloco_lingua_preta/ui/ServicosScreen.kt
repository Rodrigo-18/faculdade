package com.example.bloco_lingua_preta.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloco_lingua_preta.data.AppDatabase
import com.example.bloco_lingua_preta.data.ServicoComEmpresa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicosScreen(
    listaDeServicos: List<ServicoComEmpresa>,
    valorUnitarioBloco: Double,
    onSalvarServico: (String, String, String, String, String, String, Int, Int, Boolean, String) -> Unit,
    onSalvarValorBloco: (Double) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).appDao() }
    val listaEmpresas by dao.listarTodasEmpresas().collectAsState(initial = emptyList())

    var mostrarModalServico by remember { mutableStateOf(false) }
    var mostrarModalValor by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    var mostrarPreview by remember { mutableStateOf(false) }
    var servicoParaEditar by remember { mutableStateOf<ServicoComEmpresa?>(null) }
    var dadosPreview by remember { mutableStateOf<Map<String, String>?>(null) }
    var pesquisa by remember { mutableStateOf("") }

    val dataHoje = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var dataFiltro by remember { mutableStateOf(dataHoje) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    val servicosFiltrados = remember(listaDeServicos, dataFiltro, pesquisa) {
        listaDeServicos.filter { item ->
            val matchData = item.servico.data == dataFiltro
            val empresaNome = item.empresa?.nomeFantasia ?: ""
            val motorista = item.servico.motorista
            val placa = item.servico.placa

            val matchPesquisa = pesquisa.isBlank() ||
                    empresaNome.contains(pesquisa, ignoreCase = true) ||
                    motorista.contains(pesquisa, ignoreCase = true) ||
                    placa.contains(pesquisa, ignoreCase = true)

            matchData && matchPesquisa
        }
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
                    text = "Serviços",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Controle diário de lavagem",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { mostrarModalValor = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Valor do Serviço",
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        servicoParaEditar = null
                        mostrarModalServico = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Novo",
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alinhamento perfeito na mesma linha horizontal com verticalAlignment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dataFiltro,
                onValueChange = {},
                readOnly = true,
                label = { Text("Data") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .clickable { mostrarCalendario = true },
                trailingIcon = {
                    IconButton(onClick = { mostrarCalendario = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                label = { Text("Buscar empresa") },
//                placeholder = { Text("Buscar Empresa") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.weight(1.1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (servicosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum serviço encontrado para esta data",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(servicosFiltrados) { item ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                servicoParaEditar = item
                                mostrarModalServico = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.empresa?.nomeFantasia ?: "Sem Empresa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Motorista: ${item.servico.motorista} • Placa: ${item.servico.placa}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Blocos: ${item.servico.numeroBloco} | Pgto: ${item.servico.formaPagamento}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Text(
                                text = "R$ ${"%.2f".format(item.servico.valor)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarModalValor) {
        AlterarValorDialog(
            valorAtual = valorUnitarioBloco,
            onDismiss = { mostrarModalValor = false },
            onConfirm = { novoValor ->
                onSalvarValorBloco(novoValor)
                mostrarModalValor = false
            }
        )
    }

    if (mostrarModalServico) {
        ServicoFormDialog(
            servicoExistente = servicoParaEditar,
            dataPadrao = dataHoje,
            listaEmpresas = listaEmpresas,
            valorUnitario = valorUnitarioBloco,
            onDismiss = { mostrarModalServico = false },
            onConfirm = { bloco, placa, mot, valr, data, obs, empId, idServico, imprimir, formaPgto ->
                onSalvarServico(bloco, placa, mot, valr, data, obs, empId, idServico, imprimir, formaPgto)
                if (imprimir) {
                    val nomeEmp = listaEmpresas.find { it.id == empId }?.nomeFantasia ?: "N/A"
                    dadosPreview = mapOf("emp" to nomeEmp, "qtd" to bloco, "placa" to placa, "mot" to mot, "data" to data, "valor" to valr, "obs" to obs, "pgto" to formaPgto)
                    mostrarPreview = true
                }
                mostrarModalServico = false
            },
            onDelete = servicoParaEditar?.let { item ->
                {
                    scope.launch {
                        dao.deletarServico(item.servico)
                        mostrarModalServico = false
                    }
                }
            } ?: {}
        )
    }

    if (mostrarPreview && dadosPreview != null) {
        val d = dadosPreview!!
        AlertDialog(
            onDismissRequest = { mostrarPreview = false },
            title = { Text("Confirmar Impressão", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja imprimir a segunda via do recibo para ${d["emp"]}?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            imprimirReciboNativo(d["emp"]!!, d["qtd"]!!, d["placa"]!!, d["mot"]!!, d["data"]!!, d["valor"]!!, d["obs"]!!, d["pgto"]!!)
                        }
                        mostrarPreview = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Imprimir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarPreview = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        dataFiltro = formatter.format(Date(millis))
                    }
                    mostrarCalendario = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
@SuppressLint("MissingPermission")
fun imprimirReciboNativo(empresa: String, qtd: String, veic: String, mot: String, data: String, valor: String, obs: String, formaPgto: String) {
    val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
    if (!adapter.isEnabled) return
    val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val device = adapter.bondedDevices.firstOrNull() ?: return
    try {
        val socket = device.createRfcommSocketToServiceRecord(uuid)
        socket.connect()
        val out = socket.outputStream
        out.write(byteArrayOf(0x1B.toByte(), 0x40.toByte()))
        out.write(byteArrayOf(0x1D.toByte(), 0x21.toByte(), 0x11.toByte()))
        val agora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val msg = """
            LAVADOR DE BLOCOS
            LÍNGUA PRETA
            CPJ: 23541141/0001-17
            $agora
             --Recibo de serviço--
            ------------------------
            Empresa  : $empresa
            Blocos   : $qtd
            Valor    : R$ $valor
            Veículo  : $veic
            Motorista: $mot
            Data     : $data
            Pgto     : $formaPgto
            Obs      : $obs
            ------------------------
            
            
            ASSINATURA:
            
            
            
    
    
    
    
    
    
    
    
    
""".trimIndent()
        out.write(msg.toByteArray(Charsets.ISO_8859_1))
        out.write(byteArrayOf(0x1B.toByte(), 0x4A.toByte(), 0xC0.toByte()))
        out.flush()
        Thread.sleep(1000)
        socket.close()
    } catch (e: Exception) {
        Log.e("Impressao", "Erro: ${e.message}")
    }
}