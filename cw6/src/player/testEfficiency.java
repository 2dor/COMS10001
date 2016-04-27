package player;

import scotlandyard.*;
import graph.*;

import java.io.IOException;
import java.util.*;

public class testEfficiency {

    private final static Colour[] playerColours = {
        Colour.Black,
        Colour.Black,
        Colour.Blue,
        Colour.Green,
        Colour.Red,
        Colour.White,
        Colour.Yellow
    };

    private final static Ticket[] ticketType = {
        Ticket.Taxi,
        Ticket.Taxi,
        Ticket.Bus,
        Ticket.Underground,
        Ticket.Double,
        Ticket.Secret
    };

    private static int MAX_LIMIT = 50000000;

    public testEfficiency() {

    }

    public static void test1() {
        MoveTicket move = MoveTicket.instance(Colour.Black, Ticket.Taxi, 42);
        List<MoveTicket> allTicketMoves = new ArrayList<MoveTicket>();
        //List<Integer> allIntMoves = new ArrayList<Integer>();
        Colour player;
        Ticket ticket;
        int destination;
        System.out.println("Adding ticket instances.");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_LIMIT; ++i) {
            allTicketMoves.add(MoveTicket.instance(Colour.Black, Ticket.Taxi, 42));
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
        startTime = System.currentTimeMillis();
        System.out.println("Accessing ticket instances.");
        for (int i = 0; i < MAX_LIMIT; ++i) {
            move = allTicketMoves.get(i);
            player = move.colour;
            ticket = move.ticket;
            destination = move.target;
        }
        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
        /*************************************/

    }

    public static void test2() {
        int access = 0;
        int[] allIntMoves = new int[MAX_LIMIT];
        Colour player;
        Ticket ticket;
        int destination;
        long startTime = System.currentTimeMillis();
        System.out.println("Adding int tickets.");
        for (int i = 0; i < MAX_LIMIT; ++i) {
            allIntMoves[i] = 11042;
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
        startTime = System.currentTimeMillis();
        System.out.println("Accessing int tickets.");
        int ticketID = 0;
        for (int i = 0; i < MAX_LIMIT; ++i) {
            access = allIntMoves[i];
            player = playerColours[access/10000];
            ticketID = (access - (access / 10000) * 10000) / 1000;
            ticket = ticketType[ticketID];
            destination = access - (access/10000)*10000 - ticketID*1000;
        }

        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
    }
}
