package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{


        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        Integer availableSeats = train.getNoOfSeats()-train.getBookedTickets().size();

        if(availableSeats<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        String[] routeArr = train.getRoute().split(",");
        int to=-1, from=-1;

        for(int i=0;i<routeArr.length; i++){
            if(routeArr[i].equals(bookTicketEntryDto.getFromStation().name())){
                from = i;
            }
            if(routeArr[i].equals(bookTicketEntryDto.getToStation().name())){
                to = i;
            }
        }
        if(to==-1 || from==-1){
            throw new Exception("Invalid stations");
        }

        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement

        Ticket ticket = new Ticket();
        ticket.setTotalFare(Math.abs(300*(to-from)));
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);
        ticket.setPassengersList(new ArrayList<>());

        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        for(Integer passengerId: bookTicketEntryDto.getPassengerIds()){
            Passenger passenger = passengerRepository.findById(passengerId).get();
            ticket.getPassengersList().add(passenger);
            passenger.getBookedTickets().add(ticket);
        }
        train.getBookedTickets().add(ticket);

        Ticket savedTicket = ticketRepository.save(ticket);

       //And the end return the ticketId that has come from db

       return savedTicket.getTicketId();

    }
}
