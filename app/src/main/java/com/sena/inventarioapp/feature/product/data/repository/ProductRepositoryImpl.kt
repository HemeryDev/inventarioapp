package com.sena.inventarioapp.feature.product.data.repository

import com.sena.inventarioapp.feature.product.data.local.ProductDao
import com.sena.inventarioapp.feature.product.data.mapper.toDomain
import com.sena.inventarioapp.feature.product.data.mapper.toEntity
import com.sena.inventarioapp.feature.product.domain.model.Product
import com.sena.inventarioapp.feature.product.domain.repository.ProductRepository
import com.sena.inventarioapp.feature.product.domain.repository.SyncReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> =
        dao.observeVisible().map { rows -> rows.map { it.toDomain() } }

    override suspend fun findById(id: String): Product? =
        dao.find(id)?.takeUnless { it.isDeleted }?.toDomain()

    override suspend fun save(product: Product) {
        // Guardamos primero en Room: se mantiene el modo offline.
        dao.upsert(product.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun delete(id: String) {
        val current = dao.find(id) ?: return
        // Tombstone: no borrar fisicamente antes de sincronizar.
        dao.upsert(current.copy(
            isDeleted = true,
            pending = true,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun syncNow(): SyncReport {
        // Sincronizacion pendiente de conexion remota en Fase C
        return SyncReport(0, 0)
    }
}
