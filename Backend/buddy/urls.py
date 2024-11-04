from django.urls import path, include
from . import views

urlpatterns = [
    path('users/<int:id>/', views.users, name='users'),
    path('users/', views.users_no_id, name='users'),
    path('messages/', views.messages, name='messages'),
    path('friend_requests/', views.friend_requests, name='friend_requests'),
    path('friends/', views.friends, name='friends'),
    path('register/', views.register, name='register'),
    path('login/', views.login, name='login'),
    path('statuses/', views.statuses, name='statuses'),
    path('advertisements/', views.advertisements, name='advertisements'),
    path('ranks/', views.ranks, name='ranks'),
    path('games/', views.games, name='games')
    ]
