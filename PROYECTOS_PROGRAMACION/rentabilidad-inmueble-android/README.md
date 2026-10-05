# Rentabilidad Inmueble — Android

Aplicación Android en Kotlin + Jetpack Compose que reproduce el modelo de cálculo del Excel `rentabilidad viviendas.xlsx`.

## Qué reproduce

El motor se ha reconstruido a partir de las fórmulas reales del Excel, no de una aproximación visual.

Entradas principales:
- Entrada (%)
- Coste de compra
- Reformas / arreglos
- Comisión de agencia
- Alquiler mensual
- Años y TIN de tres hipotecas
- IRPF marginal
- Porcentaje del valor de construcción

Además, los gastos que en el Excel estaban escritos como importes fijos se pueden editar desde la app para reutilizar el modelo con otras viviendas:
- Seguro de impago
- Basuras
- Seguro de hogar
- Seguro de vida
- Comunidad
- IBI

Constantes que conserva el Excel:
- Notario / registro / tasación / gestoría: 2 %
- ITP / IVA: 7 %
- Mantenimiento: 5 % del alquiler anual
- Periodos vacíos: 5 % del alquiler anual
- Deducción vivienda habitual: 60 %
- Amortización fiscal: 3 %

## Resultados

La app muestra:
- Coste total
- Cash necesario para compra
- Cash total con reforma
- Rentabilidad bruta
- Gastos anuales
- Beneficio antes de impuestos (AI)
- Rentabilidad neta AI
- Beneficio neto después de impuestos (DI)
- Rentabilidad Neta (DI)
- CASHFLOW AI y DI, anual y mensual
- ROCE y años
- Cash-on-Cash Return y años
- Los tres escenarios hipotecarios con capital, intereses y cuota mensual

La Hipoteca 1 es el escenario que el Excel usa para calcular intereses, amortización de principal, cashflow, ROCE y Cash-on-Cash. Las hipotecas 2 y 3 son comparativas.

## Verificación contra el Excel original

Con los valores de ejemplo del archivo:
- Coste compra: 60.000 €
- Entrada: 10 %
- Reformas: 1.000 €
- Alquiler: 490 €/mes
- Hipoteca 1: 30 años al 2,44 %

Los tests comprueban, entre otros:
- Coste total: 66.400,00 €
- Rentabilidad bruta: 8,86 %
- Beneficio AI: 3.862,16 €/año
- Rentabilidad Neta AI: 5,82 %
- Beneficio Neto DI: 3.486,54 €/año
- Rentabilidad Neta DI: 5,25 %
- CASHFLOW DI: 1.686,54 €/año · 140,54 €/mes
- ROCE: 31,15 %
- Cash-on-Cash Return: 16,63 %
- Cuota Hipoteca 1: 211,68 €/mes

También se conserva el ejemplo de 45.000 € financiando el 90 % a 30 años y 2,44 %, que da 158,76 €/mes.

## Interfaz móvil

La pantalla se divide en tres pestañas:
1. **Resumen**: entradas principales y KPIs.
2. **Gastos**: gastos anuales, fiscalidad y constantes.
3. **Hipotecas**: comparación de las tres ofertas.

Los campos editables usan fondo amarillo, como en el Excel. Los KPIs usan un semáforo rojo/amarillo/verde para lectura rápida.

## Build

GitHub Actions ejecuta los tests y genera un APK debug instalable como artifact `rentabilidad-inmueble-debug-apk`.

Tecnología:
- Kotlin integrado de AGP 9
- Jetpack Compose
- Material 3
- Android Gradle Plugin 9.4.1
- Compose BOM 2026.09.00
- JDK 17
