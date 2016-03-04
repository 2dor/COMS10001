package scotlandyard;

public abstract class Move {
    public final Colour colour;
    /**
     * Constructs a new Move object.
     *
     * @param colour the colour of the player whose turn is now.
     */
    protected Move(Colour colour) {
        this.colour = colour;
    }

    @Override
    public String toString() {
        return this.colour.toString();
    }
}
