/**
 * x x ┌─────────────────┐ ┌─────────┐ x x ┌────────┐ ┌─────────────────┐ │products_on_hand ├────►
 * products│ x x │products├──────►products_on_hand │ └─────────────────┘ └───────┬─┘ x x └──────┬─┘
 * └─────────────────┘ │ x ┌──────┐ x │ ├─────────────►order ├──────────────► │ x └──────┘ x │
 * ┌──────┴──┐ x x ┌──────┴───┐ │customers│ x x │customers │ └─────────┘ x x └──────────┘ x x
 * ─────────────────────────────────────────────────────────────────────────────────── x x front
 * stage x middle stage x end stage x x there are three handlers :
 * frontStageTable\middleStageTable\backStageTable 2022-12-15
 */
package com.kronos.jobgraph.physic.operator.handler;
