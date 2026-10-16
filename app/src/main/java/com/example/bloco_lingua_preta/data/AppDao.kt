package com.example.bloco_lingua_preta.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirEmpresa(empresa: Empresa)

    @Update
    suspend fun editarEmpresa(empresa: Empresa)

    @Query("SELECT * FROM empresas ORDER BY nomeFantasia ASC")
    fun listarTodasEmpresas(): Flow<List<Empresa>>

    @Query("SELECT * FROM empresas WHERE id = :id LIMIT 1")
    suspend fun obterEmpresaPorId(id: Int): Empresa?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirServico(servico: Servico)


    @Update
    suspend fun atualizarServico(servico: Servico)

    @Query("DELETE FROM servicos WHERE id = :id")
    suspend fun excluirServicoPorId(id: Int)

    @Transaction
    @Query("SELECT * FROM servicos ORDER BY id DESC")
    fun listarTodosServicos(): Flow<List<ServicoComEmpresa>>

    @Transaction
    @Query("SELECT * FROM servicos WHERE data = :data ORDER BY id DESC")
    fun filtrarServicosPorData(data: String): Flow<List<ServicoComEmpresa>>

    @Transaction
    @Query("SELECT * FROM servicos WHERE data LIKE :mesPattern ORDER BY data ASC")
    fun obterServicosDoMes(mesPattern: String): Flow<List<ServicoComEmpresa>>

    // --- Configuração ---

    @Query("SELECT * FROM configuracoes WHERE chave = :chave LIMIT 1")
    suspend fun obterConfiguracao(chave: String): Configuracao?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarConfiguracao(configuracao: Configuracao)

    @Query("SELECT valor FROM configuracoes WHERE chave = 'valor_bloco' LIMIT 1")
    suspend fun obterValorUnitarioBloco(): String?

    @Query("DELETE FROM servicos WHERE data LIKE '%' || :mesAno")
    suspend fun deletarServicosPorMesAno(mesAno: String)

    @Delete
    suspend fun deletarServico(servico: Servico)
}