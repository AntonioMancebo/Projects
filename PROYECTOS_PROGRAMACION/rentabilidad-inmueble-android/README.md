# Rentabilidad Inmueble — Android

Aplicación Android en Kotlin + Jetpack Compose para trasladar al móvil el Excel de cálculo de rentabilidad inmobiliaria.

## Estado actual

La rama contiene una primera versión funcional de la interfaz y del motor hipotecario.

### Fórmulas ya verificadas contra los números facilitados

Para un precio de compra de 45.000 €, entrada del 10 %, financiación del 90 % y 30 años:

- TIN 2,44 %:
  - Capital financiado: 40.500,00 €
  - Intereses totales: 16.654,83 €
  - Capital anual medio: 1.350,00 €
  - Interés anual medio: 555,16 €
  - Capital mensual medio: 112,50 €
  - Interés mensual medio: 46,26 €
  - Cuota mensual: 158,76 €

- TIN 1,55 %:
  - Capital financiado: 40.500,00 €
  - Intereses totales: 10.169,08 €
  - Interés anual medio: 338,97 €
  - Interés mensual medio: 28,25 €
  - Cuota mensual: 140,75 €

La cuota usa amortización francesa estándar. Los valores "capital/interés anual y mensual" reproducen el criterio observado en el Excel: total dividido entre años y meses.

## Entradas visibles

- Coste compra
- Entrada (%)
- Reformas / arreglos
- Comisión de agencia
- Alquiler mensual
- Años y TIN para tres escenarios hipotecarios

## Salidas

- Coste total de adquisición
- Dinero aportado
- Alquiler anual
- Rentabilidad Neta (DI)
- Cash-on-Cash Return
- Financiación
- Capital financiado
- Intereses totales
- Capital e intereses medios anual/mensual
- Cuota mensual

## Pendiente imprescindible

El Excel original no está disponible en esta conversación ni en la biblioteca de archivos encontrada. Por tanto, las fórmulas de:

- Rentabilidad Neta (DI)
- Cash-on-Cash Return
- umbrales exactos rojo / amarillo / verde

están deliberadamente aisladas y marcadas como provisionales en InvestmentCalculator.kt.

En cuanto se disponga del .xlsx original hay que leer:
1. celdas amarillas y sus referencias;
2. fórmulas exactas de todas las celdas calculadas;
3. reglas de formato condicional;
4. si existen gastos/impuestos/IBI/comunidad/seguros/vacancia no visibles en el fragmento;
5. cualquier redondeo especial.

Después se sustituyen únicamente esas funciones manteniendo la interfaz.

## Tecnología

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin 9.4.1
- Compose BOM 2026.09.00
- JDK 17
