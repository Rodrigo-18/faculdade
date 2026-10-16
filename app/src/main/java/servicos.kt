package com.example.bloco_lingua_preta.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicos")
data class Servico(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "empresa_id")
    val empresaId: Int,

    @NonNull
    @ColumnInfo(name = "numero_bloco")
    val numeroBloco: Int,

    @NonNull
    @ColumnInfo(name = "placa")
    val placa: String,

    @NonNull
    @ColumnInfo(name = "motorista")
    val motorista: String,

    @NonNull
    @ColumnInfo(name = "data")
    val data: String,

    @NonNull
    @ColumnInfo(name = "forma_pagamento")
    val formaPagamento: String,

    @NonNull
    @ColumnInfo(name = "valor")
    val valor: Double,

    @NonNull
    @ColumnInfo(name = "observacao")
    val observacao: String
)