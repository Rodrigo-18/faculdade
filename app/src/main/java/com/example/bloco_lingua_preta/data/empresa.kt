package com.example.bloco_lingua_preta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresas")
data class Empresa(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomeFantasia: String,
    val cnpj: String
)