/*
* Copyright 2010-2012 Ning, Inc.
*
* Ning licenses this file to you under the Apache License, version 2.0
* (the 'License'); you may not use this file except in compliance with the
* License.  You may obtain a copy of the License at:
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an 'AS IS' BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
* License for the specific language governing permissions and limitations
* under the License.
*/

describe('The set implementation', function () {
    it('should be able to create sets from objects', function() {
        var objs = [
            {
                name: 'banana',
                color: 'yellow'
            },
            {
                name: 'apple',
                color: 'green'
            },
            {
                name: 'apple',
                color: 'yellow'
            }
        ];

        var mySet = Set.makeSet(objs, 'name');
        expect(Set.size(mySet)).toEqual(2);
        expect(mySet.banana).toBeTruthy();
        expect(mySet.apple).toBeTruthy();
        expect(mySet.yello).toBeFalsy();
        expect(mySet.green).toBeFalsy();

        mySet = Set.makeSet(objs, 'color');
        expect(Set.size(mySet)).toEqual(2);
        expect(mySet.banana).toBeFalsy();
        expect(mySet.apple).toBeFalsy();
        expect(mySet.yellow).toBeTruthy();
        expect(mySet.green).toBeTruthy();

        expect(Set.makeSet({}, null)).toEqual({});
        expect(Set.makeSet(null, null)).toEqual({});
        expect(Set.makeSet(undefined, null)).toEqual({});
    });

    it('should be able to add an element to a set', function() {
        var mySet = Set.makeSet();
        expect(Set.size(mySet)).toBe(0);

        Set.add(mySet, 'foo');
        expect(Set.size(mySet)).toBe(1);
        expect(Set.contains(mySet, 'foo')).toBeTruthy();

        Set.add(mySet, 'foo');
        expect(Set.size(mySet)).toBe(1);
        expect(Set.contains(mySet, 'foo')).toBeTruthy();

        Set.add(mySet, null);
        expect(Set.size(mySet)).toBe(1);
        expect(Set.contains(mySet, 'foo')).toBeTruthy();

        Set.add(mySet, 'bar');
        expect(Set.size(mySet)).toBe(2);
        expect(Set.contains(mySet, 'foo')).toBeTruthy();
        expect(Set.contains(mySet, 'bar')).toBeTruthy();
    });

    it('should be able to convert a set to an array', function() {
        var mySet = Set.makeSet();
        Set.add(mySet, 'foo');
        Set.add(mySet, 'bar');

        var elts = Set.elements(mySet);
        expect(elts.length).toBe(2);
        expect(elts[0]).toBe('foo');
        expect(elts[1]).toBe('bar');
    });

    it('should be able to check if an element is in a set', function() {
        var objs = [
            {
                name: 'banana',
                color: 'yellow'
            }
        ];

        var mySet = Set.makeSet(objs, 'name');
        expect(Set.contains(mySet, 'banana')).toBeTruthy();
        expect(Set.contains(mySet, 'yellow')).toBeFalsy();

        expect(Set.contains({}, 'yellow')).toBeFalsy();
        expect(Set.contains(null, 'yellow')).toBeFalsy();
        expect(Set.contains(undefined, 'yellow')).toBeFalsy();
    });

    it('should be able to compute the size of a set', function() {
        var objs = [
            {
                name: 'banana',
                color: 'yellow'
            }
        ];

        var mySet = Set.makeSet(objs, 'name');
        expect(Set.size(mySet)).toEqual(1);

        expect(Set.size(null)).toEqual(0);
        expect(Set.size({})).toEqual(0);
        expect(Set.size(undefined)).toEqual(0);
    });

    it('should be able to compute the symmetric difference between two sets', function() {
        var objs1 = [
            {
                name: 'banana',
                color: 'yellow'
            },
            {
                name: 'apple',
                color: 'green'
            },
            {
                name: 'apple',
                color: 'yellow'
            }
        ];
        var objs2 = [
            {
                name: 'apple',
                color: 'red'
            },
            {
                name: 'apple',
                color: 'yellow'
            }
        ]

        var diff = Set.symdiff(Set.makeSet(objs1, 'name'), Set.makeSet(objs2, 'name'));
        expect(Set.size(diff)).toEqual(1);
        expect(diff.banana).toBeTruthy();
        expect(diff.apple).toBeFalsy();
        expect(diff.yello).toBeFalsy();
        expect(diff.green).toBeFalsy();

        diff = Set.symdiff(Set.makeSet(objs2, 'name'), Set.makeSet(objs1, 'name'));
        expect(Set.size(diff)).toEqual(1);
        expect(diff.banana).toBeTruthy();
        expect(diff.apple).toBeFalsy();
        expect(diff.yello).toBeFalsy();
        expect(diff.green).toBeFalsy();
    });

    it('should be able to test whether two sets are equal', function() {
        var objs = [
            {
                name: 'banana',
                color: 'yellow'
            },
            {
                name: 'apple',
                color: 'green'
            },
            {
                name: 'apple',
                color: 'yellow'
            }
        ];

        var mySet = Set.makeSet(objs, 'name');
        expect(Set.equals(mySet, mySet)).toBeTruthy();
        expect(Set.equals(mySet, Set.makeSet([], null))).toBeFalsy();
        expect(Set.equals(Set.makeSet([], null), mySet)).toBeFalsy();
    });
});