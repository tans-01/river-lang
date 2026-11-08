class Release extends RuntimeException {
    final Object value;

    Release(Object value) {
        this.value = value;
    }
}