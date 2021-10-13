enum Token {
    MCDEFN {
        @Override
        public String toString() {
            return "MACRO";
        }
    },

    MCEND {
        @Override
        public String toString() {
            return "ENDM";
        }
    }
}
