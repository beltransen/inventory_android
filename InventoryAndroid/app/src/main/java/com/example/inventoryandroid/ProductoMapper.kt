package com.example.inventoryandroid

fun ProductoEntity.toDomain(): Producto {
    return Producto(
        productoId = this.productoId,
        nombre = this.nombre,
        foto = this.foto,
        categoria = this.categoria,
        precio = this.precio,
        cantidadAñadida = this.cantidadAñadida,
        ultimaActualizacion = this.ultimaActualizacion,
        activo = this.activo
    )
}

fun Producto.toEntity(): ProductoEntity {
    return ProductoEntity(
        productoId = this.productoId,
        nombre = this.nombre,
        foto = this.foto,
        categoria = this.categoria,
        precio = this.precio,
        cantidadAñadida = this.cantidadAñadida,
        ultimaActualizacion = this.ultimaActualizacion,
        activo = this.activo
    )
}

fun ProductoDTO.toDomain(): Producto {
    return Producto(
        productoId = this.productoId,
        nombre = this.nombre,
        foto = this.foto,
        categoria = this.categoria,
        precio = this.precio,
        cantidadAñadida = this.cantidadAñadida,
        ultimaActualizacion = this.ultimaActualizacion,
        activo = this.activo
    )
}

fun Producto.toDTO(): ProductoDTO {
    return ProductoDTO(
        productoId = this.productoId,
        nombre = this.nombre,
        foto = this.foto,
        categoria = this.categoria,
        precio = this.precio,
        cantidadAñadida = this.cantidadAñadida,
        ultimaActualizacion = this.ultimaActualizacion,
        activo = this.activo
    )
}

