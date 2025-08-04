document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll(
      ".add-to-bucket, .increment-amount, .decrement-amount"
  ).forEach(el => el.addEventListener('click', () => {
    window.location.reload();
  }));
});