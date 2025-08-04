function decrementBucket(bucketId, productId) {
  const changeAmountControlsEl = document.getElementById(
      `product-${productId}-controls-change`
  )
  const counterEl = changeAmountControlsEl.querySelector('.counter')
  const count = Number.parseInt(counterEl.textContent)
  if (count > 1) {
    changeAmount(bucketId, productId, false)
    counterEl.textContent = count - 1 + ''
  } else {
    removeProduct(bucketId, productId)

    const changeAmountControlsEl = document.getElementById(
        `product-${productId}-controls-change`
    )
    changeAmountControlsEl.style.display = 'none'

    const putInBucketControl = document.getElementById(
        `product-${productId}-controls-put`
    )
    putInBucketControl.style.display = 'block'
  }
}

function incrementBucket(bucketId, productId) {
  const changeAmountControlsEl = document.getElementById(
      `product-${productId}-controls-change`
  )
  const counterEl = changeAmountControlsEl.querySelector('.counter')
  const count = Number.parseInt(counterEl.textContent)
  changeAmount(bucketId, productId, true)
  counterEl.textContent = count + 1 + ''
}

function putProductInBucket(bucketId, productId) {
  addProduct(bucketId, productId)
  const changeAmountControlsEl = document.getElementById(
      `product-${productId}-controls-change`
  )
  const counterEl = changeAmountControlsEl.querySelector('.counter')
  counterEl.textContent = '1'
  changeAmountControlsEl.style.display = 'flex'

  const putInBucketControl = document.getElementById(
      `product-${productId}-controls-put`
  )
  putInBucketControl.style.display = 'none'
}

function addProduct(bucketId, productId) {
  fetch(`/bucket/${bucketId}/${productId}`, {
    method: 'POST',
  })
  .then(response => {
    if (!response.ok) throw new Error('Ошибка при добавлении продукта');
    console.log('Продукт добавлен');
  })
  .catch(error => console.error(error));
}

function removeProduct(bucketId, productId) {
  fetch(`/bucket/${bucketId}/${productId}`, {
    method: 'DELETE',
  })
  .then(response => {
    if (!response.ok) throw new Error('Ошибка при удалении продукта');
    console.log('Продукт удалён');
  })
  .catch(error => console.error(error));
}

function changeAmount(bucketId, productId, increase) {
  const url = new URL(`/bucket/${bucketId}/${productId}`, window.location.origin);
  url.searchParams.append('increase', increase);

  fetch(url.toString(), {
    method: 'PUT',
  })
  .then(response => {
    if (!response.ok) throw new Error('Ошибка при изменении количества');
    console.log('Количество изменено');
  })
  .catch(error => console.error(error));
}