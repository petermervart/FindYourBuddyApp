from django.db import models
import uuid

class Users(models.Model):
    iduser = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    name = models.CharField(max_length=35, blank=False, null=False)
    email = models.CharField(max_length=35, blank=False, null=False)
    password = models.TextField(null=False, blank=False)
    description = models.TextField(null=True, blank=True)
    picture = models.TextField(null=True, blank=True)
    created_at = models.DateTimeField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'users'

class Friend_requests(models.Model):
    idfriend_requests = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    sender = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="sender")
    reciever = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="reciever")
    created_at = models.DateTimeField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'friend_requests'

class Friends(models.Model):
    idfriends = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    user_1 = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="user_1", db_column="user_1")
    user_2 = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="user_2", db_column="user_2")
    created_at = models.DateTimeField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'friends'

class Messages(models.Model):
    idmessages = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    sender = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="message_sender")
    reciever = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="message_reciever")
    text = models.TextField(null=True, blank=True)
    created_at = models.DateTimeField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'messages'

class Statuses(models.Model):
    idstatuses = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    owner = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="status_owner", db_column="owner_id")
    text = models.TextField(null=False, blank=False)
    created_at = models.DateTimeField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'statuses'

class Games(models.Model):
    idgames = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    name = models.CharField(max_length=80, blank=False, null=False)
    description = models.TextField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'games'

class Ranks(models.Model):
    idranks = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    game = models.ForeignKey(Games, models.DO_NOTHING, blank=False, null=False, related_name="rank_game",  db_column="game_id")
    name = models.CharField(max_length=80, blank=False, null=False)
    tier = models.CharField(max_length=80, blank=False, null=False)

    class Meta:
        managed = False
        db_table = 'ranks'

class Advertisements(models.Model):
    idadvertisement = models.AutoField(primary_key=True, editable=False, blank=False, null=False)
    owner = models.ForeignKey(Users, models.DO_NOTHING, blank=False, null=False, related_name="advertisement_owner",db_column="owner_id")
    rank = models.ForeignKey(Ranks, models.DO_NOTHING, blank=False, null=False, related_name="advertisement_rank_id", db_column="rank_id")
    game = models.ForeignKey(Games, models.DO_NOTHING, blank=False, null=False, related_name="advertisement_game", db_column="game_id")
    text = models.TextField(null=False, blank=False)
    created_at = models.DateTimeField(null=False, blank=False)

    class Meta:
        managed = False
        db_table = 'advertisements'
