
package com.example.bloco_lingua_preta.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "configuracoes")
data class Configuracao(
    @PrimaryKey val chave: String,
    val valor: String
)