package com.example.bloco_lingua_preta.data

import androidx.room.Embedded
import androidx.room.Relation


data class ServicoComEmpresa(
    @Embedded
    val servico: Servico,

    @Relation(
        parentColumn = "empresa_id",
        entityColumn = "id"
    )
    val empresa: Empresa?
)