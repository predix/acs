package com.ge.predix.acs.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.validation.Valid;

public class ValidList<E> implements List<E> {

    @Valid
    private List<E> list;

    public ValidList() {
        this.list = new ArrayList<E>();
    }

    public ValidList(final List<E> list) {
        this.list = list;
    }

    public List<E> getList() {
        return this.list;
    }

    public void setList(final List<E> list) {
        this.list = list;
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public boolean add(final E arg0) {
        return this.list.add(arg0);
    }

    @Override
    public void add(final int arg0, final E arg1) {
        this.list.add(arg0, arg1);

    }

    @Override
    public boolean addAll(final Collection<? extends E> arg0) {
        return this.list.addAll(arg0);

    }

    @Override
    public boolean addAll(final int arg0, final Collection<? extends E> arg1) {
        return this.list.addAll(arg0, arg1);
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public boolean contains(final Object arg0) {
        return this.list.contains(arg0);
    }

    @Override
    public boolean containsAll(final Collection<?> arg0) {
        return this.list.containsAll(arg0);
    }

    @Override
    public E get(final int arg0) {
        return this.list.get(arg0);

    }

    @Override
    public int indexOf(final Object arg0) {
        return this.list.indexOf(arg0);
    }

    @Override
    public Iterator<E> iterator() {

        return this.list.iterator();
    }

    @Override
    public int lastIndexOf(final Object arg0) {
        return this.list.lastIndexOf(arg0);
    }

    @Override
    public ListIterator<E> listIterator() {
        return this.list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(final int arg0) {
        return this.list.listIterator(arg0);
    }

    @Override
    public boolean remove(final Object arg0) {
        return this.list.remove(arg0);
    }

    @Override
    public E remove(final int arg0) {
        return this.list.remove(arg0);
    }

    @Override
    public boolean removeAll(final Collection<?> arg0) {
        return this.list.removeAll(arg0);
    }

    @Override
    public boolean retainAll(final Collection<?> arg0) {
        return this.list.removeAll(arg0);
    }

    @Override
    public E set(final int arg0, final E arg1) {
        return this.list.set(arg0, arg1);
    }

    @Override
    public List<E> subList(final int arg0, final int arg1) {
        return this.list.subList(arg0, arg1);
    }

    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] arg0) {
        return this.list.toArray(arg0);
    }

}