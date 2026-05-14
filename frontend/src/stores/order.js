import { defineStore } from 'pinia'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orderList: [],
    currentOrder: null
  }),
  actions: {
    setOrderList(orderList) {
      this.orderList = orderList
    },
    setCurrentOrder(order) {
      this.currentOrder = order
    }
  }
})
