# Mapa de fórmulas del Excel original

Este documento deja trazabilidad entre `Hoja1` del Excel original y `InvestmentCalculator.kt`.

## Compra

| Celda | Concepto | Fórmula |
|---|---|---|
| C6 | Entrada € | `=C7*C5` |
| C10 | Notario/registro/tasación/gestoría | `=C7*0.02` |
| C11 | ITP/IVA | `=C7*0.07` |
| C13 | Coste total | `=SUM(C7:C11)` |
| C16 | Alquiler anual | `=C15*12` |
| C18 | Rentabilidad bruta | `=C16/C13` |

## Hipotecas

La financiación se calcula como `100% - Entrada (%)`.

Hipoteca 1:
- F9 capital financiado: `=$C$7*F8`
- G9 intereses totales: `=-CUMIPMT(G7/12,G6*12,F9,1,G6*12,0)`
- F10 capital anual medio: `=F9/G6`
- G10 interés anual medio: `=G9/G6`
- F11 capital mensual medio: `=F10/12`
- G11 interés mensual medio: `=G10/12`
- G13 cuota mensual: `=F11+G11`

Hipotecas 2 y 3 repiten la misma estructura.

## Cash necesario

- G17: `=(C11+C10+C6+C9)`
- G18: `=C8`
- G19: `=SUM(G17:G18)`

## Gastos y beneficio antes de impuestos

- C20 ingresos: `=C16`
- C27 mantenimiento: `=-(0.05*C20)`
- C28 periodos vacío: `=-(0.05*C20)`
- C29 intereses hipoteca: `=-G10`
- C31 beneficio AI: `=SUM(C20:C29)`
- D31 beneficio AI mensual: `=C31/12`
- C32 rentabilidad neta AI: `=C31/C13`

Los importes de seguro de impago, basuras, seguro hogar, seguro vida, comunidad e IBI estaban introducidos manualmente en C21:C26.

## Fiscalidad

- C35 base tras deducción del 60 %: `=(C31-C37)*0.4`
- C36 IRPF: `=-(C35*A36)`
- C37 amortización anual: `=3%*(A37*C7+SUM(C8:C11))`
- C39 beneficio neto DI: `=C31+C36`
- D39 beneficio neto DI mensual: `=C39/12`
- C40 rentabilidad neta DI: `=(C31+C36)/C13`

## Cashflow y retornos

- C42 CASHFLOW AI: `=C31-F10`
- D42: `=C42/12`
- C43 CASHFLOW DI: `=C39-F10`
- D43: `=C43/12`
- C44 ROCE: `=C31/(C6+C8+C9+C10+C11)`
- C45 ROCE años: `=C6/(C6*C44)` = `1/ROCE`
- C46 Cash-on-Cash Return: `=C42/(C6+C8+C9+C10+C11)`
- C47 COCR años: `=(C6+C8+C9+C10+C11)/((C6+C8+C9+C10+C11)*C46)` = `1/COCR`

## Formato condicional observado

- Rentabilidad bruta (C18):
  - rojo: < 7 %
  - amarillo: 7,01–7,99 %
  - verde: > 8 %
- Rentabilidad Neta DI (C40):
  - rojo: < 5 %
  - verde: > 5 %
- Cash-on-Cash (C46):
  - verde: > 7 %

Para hacer más útil la lectura en móvil, la app completa una franja amarilla intermedia en Rentabilidad Neta DI y Cash-on-Cash sin cambiar ninguna fórmula financiera.
