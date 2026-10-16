package com.example.bloco_lingua_preta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bloco_lingua_preta.data.*
import com.example.bloco_lingua_preta.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var estaLogado by rememberSaveable { mutableStateOf(false) }

            if (estaLogado) {
                MainScreen(onLogout = { estaLogado = false })
            } else {
                LoginScreen(onLoginSuccess = { estaLogado = true })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).appDao() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var subTelaAtiva by remember { mutableStateOf("empresas") }
    var mostrarDialogExclusao by remember { mutableStateOf(false) }
    val listaEmpresas by dao.listarTodasEmpresas().collectAsState(initial = emptyList())
    val listaServicos by dao.listarTodosServicos().collectAsState(initial = emptyList())
    var valorUnitarioBloco by remember { mutableStateOf(35.0) }

    LaunchedEffect(Unit) {
        val valorSalvo = dao.obterValorUnitarioBloco()
        if (!valorSalvo.isNullOrBlank()) {
            valorUnitarioBloco = valorSalvo.toDoubleOrNull() ?: 35.0
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bloco Língua Preta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Painel de Gestão",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Business, contentDescription = null) },
                        label = { Text("Empresas", fontWeight = FontWeight.Medium) },
                        selected = subTelaAtiva == "empresas",
                        onClick = { subTelaAtiva = "empresas"; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        label = { Text("Serviços", fontWeight = FontWeight.Medium) },
                        selected = subTelaAtiva == "servicos",
                        onClick = { subTelaAtiva = "servicos"; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                        label = { Text("Relatórios", fontWeight = FontWeight.Medium) },
                        selected = subTelaAtiva == "relatorio",
                        onClick = { subTelaAtiva = "relatorio"; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                        label = { Text("Limpar Mês/Ano") },
                        selected = false,
                        onClick = { mostrarDialogExclusao = true; scope.launch { drawerState.close() } },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sair do Sistema", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bloco Língua Preta") },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") } }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (subTelaAtiva) {
                    "servicos" -> ServicosScreen(
                        listaDeServicos = listaServicos,
                        valorUnitarioBloco = valorUnitarioBloco,
                        onSalvarServico = { b, p, m, v, d, o, eId, idS, imp, formaPgto ->
                            scope.launch {
                                dao.inserirServico(Servico(id = idS, empresaId = eId, numeroBloco = b.toIntOrNull() ?: 0, placa = p, motorista = m, data = d, formaPagamento = formaPgto, valor = v.toDoubleOrNull() ?: 0.0, observacao = o))

                                if (imp) {
                                    val nome = listaEmpresas.find { it.id == eId }?.nomeFantasia ?: "Empresa"
                                    scope.launch(Dispatchers.IO) {
                                        imprimirReciboNativo(nome, b, p, m, d, v, o, formaPgto)
                                    }
                                }
                            }
                        },
                        onSalvarValorBloco = { novo ->
                            scope.launch {
                                dao.salvarConfiguracao(Configuracao("valor_bloco", novo.toString()))
                                valorUnitarioBloco = novo
                            }
                        }
                    )
                    "empresas" -> EmpresasScreen(listaEmpresas) { e -> scope.launch { dao.inserirEmpresa(e) } }
                    "relatorio" -> RelatoriosScreen(listaServicos, listaEmpresas)
                }
            }
        }
    }

    if (mostrarDialogExclusao) {
        ExclusaoDialog(
            onConfirm = { mes, ano ->
                scope.launch {
                    val format = "/$mes/$ano"
                    dao.deletarServicosPorMesAno(format)
                }
                mostrarDialogExclusao = false
            },
            onDismiss = { mostrarDialogExclusao = false }
        )
    }
}